package com.salcedo.rapbot.driver;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.model.Uri;
import com.salcedo.rapbot.locomotion.Motor;
import com.salcedo.rapbot.locomotion.MotorActor;
import com.salcedo.rapbot.locomotion.MotorRequest;
import com.salcedo.rapbot.locomotion.MotorServiceFactory;
import com.salcedo.rapbot.snapshot.StartSnapshotMessage;

import java.awt.event.KeyEvent;

public final class DriverActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Uri motorNode;
    private final DriverStrategy driverStrategy;
    private ActorRef motors;

    public DriverActor(final Uri motorNode, final DriverStrategy driverStrategy) {
        this.motorNode = motorNode;
        this.driverStrategy = driverStrategy;
    }

    public static <T> Props props(final Uri motorNode, final DriverStrategy<T> driverStrategy) {
        return Props.create(DriverActor.class, motorNode, driverStrategy);
    }

    @Override
    public void preStart() throws Exception {
        motors = getContext().actorOf(
                MotorActor.props(MotorServiceFactory.http(getContext().getSystem(), motorNode)),
                "motors"
        );

        context().system().eventStream().subscribe(self(), KeyEvent.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(KeyEvent.class, this::drive)
                .build();
    }

    private void drive(final KeyEvent keyEvent) {
        DriveState state = driverStrategy.drive(keyEvent);

        context().system().eventStream().publish(new StartSnapshotMessage());
    }

    private MotorRequest createStopRequest() {
        final Motor backLeftMotor = Motor.builder()
                .withReleaseCommand()
                .withBackLeftLocation()
                .withSpeed(0)
                .build();
        final Motor backRightMotor = Motor.builder()
                .withReleaseCommand()
                .withBackRightLocation()
                .withSpeed(0)
                .build();

        return createMotorRequest(backLeftMotor, backRightMotor);
    }

    private MotorRequest createMotorRequest(final Motor backLeftMotor, final Motor backRightMotor) {
        return MotorRequest.builder()
                .addMotor(backLeftMotor)
                .addMotor(backRightMotor)
                .build();
    }

    private MotorRequest createForwardRequest(final int leftSpeed, final int rightSpeed) {
        final Motor backLeftMotor = Motor.builder()
                .withForwardCommand()
                .withBackLeftLocation()
                .withSpeed(leftSpeed)
                .build();
        final Motor backRightMotor = Motor.builder()
                .withForwardCommand()
                .withBackRightLocation()
                .withSpeed(rightSpeed)
                .build();

        return createMotorRequest(backLeftMotor, backRightMotor);
    }

    private MotorRequest createReverseRequest() {
        final Motor backLeftMotor = Motor.builder()
                .withBackwardCommand()
                .withBackLeftLocation()
                .withSpeed(255)
                .build();
        final Motor backRightMotor = Motor.builder()
                .withBackwardCommand()
                .withBackRightLocation()
                .withSpeed(255)
                .build();

        return createMotorRequest(backLeftMotor, backRightMotor);
    }
}
