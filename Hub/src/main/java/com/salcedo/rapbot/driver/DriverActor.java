package com.salcedo.rapbot.driver;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.sense.Orientation;
import com.salcedo.rapbot.sense.OrientationRequest;
import com.salcedo.rapbot.snapshot.ObjectSnapshotMessage;
import com.salcedo.rapbot.snapshot.RegisterSubSystemMessage;
import com.salcedo.rapbot.snapshot.StartSnapshotMessage;
import com.salcedo.rapbot.snapshot.TakeSnapshotMessage;

import java.awt.event.KeyEvent;

import static com.salcedo.rapbot.locomotion.Command.FORWARD;

public final class DriverActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final DriverStrategy<KeyEvent> driverStrategy;
    private final DriverMotorStrategy motorStrategy;
    private final ActorRef motors;
    private final ActorRef sensors;
    private DriveState desiredState;

    public DriverActor(
            final ActorRef motors,
            final ActorRef sensors,
            final DriverStrategy<KeyEvent> driverStrategy
    ) {
        this.motors = motors;
        this.sensors = sensors;
        this.driverStrategy = driverStrategy;
        this.motorStrategy = new PIDControllerDriverMotorStrategy(
                new SimplePIDController(0.25, 0.01, 0.4),
                getContext().getSystem()
        );
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
        context().system().eventStream().subscribe(self(), KeyEvent.class);

        context().watch(motors);
        context().watch(sensors);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(KeyEvent.class, this::updateDesiredState)
                .match(TakeSnapshotMessage.class, this::snapshot)
                .match(Orientation.class, this::drive)
                .match(Terminated.class, message -> terminate())
                .build();
    }

    private void terminate() {
        context().stop(self());
    }

    private void drive(final Orientation orientation) {
        motors.tell(motorStrategy.drive(orientation, desiredState), self());
    }

    private void snapshot(TakeSnapshotMessage message) {
        sender().tell(new ObjectSnapshotMessage(message.getUuid(), desiredState), self());
    }

    private void updateDesiredState(final KeyEvent keyEvent) {
        desiredState = driverStrategy.drive(keyEvent, desiredState);

        log.debug("Changed desired drive state to {}", desiredState);

        sensors.tell(new OrientationRequest(true), self());

        context().system().eventStream().publish(new StartSnapshotMessage());
    }
}
