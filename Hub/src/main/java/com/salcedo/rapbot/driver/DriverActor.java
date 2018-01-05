package com.salcedo.rapbot.driver;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.locomotion.Motor;
import com.salcedo.rapbot.locomotion.MotorRequest;
import com.salcedo.rapbot.sense.Orientation;
import com.salcedo.rapbot.sense.OrientationRequest;
import com.salcedo.rapbot.snapshot.ObjectSnapshotMessage;
import com.salcedo.rapbot.snapshot.RegisterSubSystemMessage;
import com.salcedo.rapbot.snapshot.StartSnapshotMessage;
import com.salcedo.rapbot.snapshot.TakeSnapshotMessage;

import java.awt.event.KeyEvent;

import static com.salcedo.rapbot.locomotion.Command.FORWARD;
import static java.lang.Math.PI;
import static java.lang.Math.toRadians;

public final class DriverActor extends AbstractActor {
    private static final double EPSILON = 0.0001;
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final DriverStrategy<KeyEvent> driverStrategy;
    private final ActorRef motors;
    private final ActorRef sensors;
    private final PIDController pid;
    private DriveState desiredState;
    private PIDResult pidResult;

    public DriverActor(
            final ActorRef motors,
            final ActorRef sensors,
            final DriverStrategy<KeyEvent> driverStrategy
    ) {
        this.motors = motors;
        this.sensors = sensors;
        this.driverStrategy = driverStrategy;
        this.pid = new SimplePIDController(0.25, 0.01, 0.4);
        this.pidResult = new PIDResult(0.0);
        this.desiredState = new DriveState(
                new OpenClosedRange(true, true, 0, 255),
                new OpenClosedRange(true, true, 0, 360),
                0,
                0,
                FORWARD
        );
    }

    public static Props props(
            final ActorRef motors,
            final ActorRef sensors,
            final DriverStrategy<KeyEvent> driverStrategy
    ) {
        return Props.create(DriverActor.class, motors, sensors, driverStrategy);
    }

    @Override
    public void preStart() {
        context().system().eventStream().publish(new RegisterSubSystemMessage(self()));
        context().system().eventStream().subscribe(self(), KeyEvent.class);

        context().watch(motors);
        context().watch(sensors);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(KeyEvent.class, this::drive)
                .match(TakeSnapshotMessage.class, this::snapshot)
                .match(Orientation.class, this::control)
                .match(Terminated.class, message -> terminate())
                .build();
    }

    private void terminate() {
        context().stop(self());
    }

    private void control(final Orientation orientation) {
        final double actual = toRadians(orientation.getYaw());
        final double target = toRadians(desiredState.getOrientation());
        final double adjustedActual = adjustRadians(actual, target);
        final double adjustedTarget = adjustRadians(target, actual);

        final PIDResult result = pid.step(adjustedActual, adjustedTarget, pidResult);

        if (isCloseToPi(adjustedTarget - adjustedActual)) {
            desiredState = desiredState.toggleCommand();
        }

        // TODO: Need to figure out if I have left and right swapped.
        controlMotors(result.getOutput() / (2 * PI));
    }

    private void controlMotors(final double leftToRightRatio) {
        final int adjustedLeftThrottle = desiredState.getThrottleRange()
                .bounded((int) (desiredState.getThrottle() * (1 - leftToRightRatio)));
        final int adjustedRightThrottle = desiredState.getThrottleRange()
                .bounded((int) (desiredState.getThrottle() * leftToRightRatio));
        final MotorRequest motorRequest = createMotorRequest(adjustedLeftThrottle, adjustedRightThrottle);

        motors.tell(motorRequest, self());

    }

    private MotorRequest createMotorRequest(final int leftSpeed, final int rightSpeed) {
        log.info("Moving motors with command: {}, and speed left: {}, right: {}.", desiredState.getCommand(), leftSpeed, rightSpeed);

        final Motor backLeftMotor = Motor.builder()
                .withCommand(desiredState.getCommand())
                .withBackLeftLocation()
                .withSpeed(leftSpeed)
                .build();
        final Motor backRightMotor = Motor.builder()
                .withCommand(desiredState.getCommand())
                .withBackRightLocation()
                .withSpeed(rightSpeed)
                .build();

        return MotorRequest.builder()
                .addMotor(backLeftMotor)
                .addMotor(backRightMotor)
                .build();
    }

    private double adjustRadians(double x, double y) {
        return isCloseToPi(y - x) ? x + (2 * PI) : x;
    }

    private boolean isCloseToPi(double delta) {
        return delta >= PI + EPSILON || delta >= PI - EPSILON;
    }

    private void snapshot(TakeSnapshotMessage message) {
        sender().tell(new ObjectSnapshotMessage(message.getUuid(), desiredState), self());
    }

    private void drive(final KeyEvent keyEvent) {
        desiredState = driverStrategy.drive(keyEvent, desiredState);

        log.debug("Changed desired drive state to {}", desiredState);

        sensors.tell(new OrientationRequest(true), self());

        context().system().eventStream().publish(new StartSnapshotMessage());
    }
}
