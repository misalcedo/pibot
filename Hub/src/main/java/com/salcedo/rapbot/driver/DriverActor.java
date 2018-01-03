package com.salcedo.rapbot.driver;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.EventStream;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.model.Uri;
import com.salcedo.rapbot.locomotion.*;
import com.salcedo.rapbot.snapshot.ObjectSnapshotMessage;
import com.salcedo.rapbot.snapshot.RegisterSubSystemMessage;
import com.salcedo.rapbot.snapshot.StartSnapshotMessage;
import com.salcedo.rapbot.snapshot.TakeSnapshotMessage;

import java.awt.event.KeyEvent;

public final class DriverActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Uri motorNode;
    private final DriverStrategy<KeyEvent> driverStrategy;
    private ActorRef motors;
    private DriveState currentState;

    public DriverActor(final Uri motorNode, final DriverStrategy<KeyEvent> driverStrategy) {
        this.motorNode = motorNode;
        this.driverStrategy = driverStrategy;
        this.currentState = new DriveState(
                new OpenClosedRange(true, true, 0, 100),
                new OpenClosedRange(true, true, 0, 360),
                0,
                0
        );
    }

    public static Props props(final Uri motorNode, final DriverStrategy<KeyEvent> driverStrategy) {
        return Props.create(DriverActor.class, motorNode, driverStrategy);
    }

    @Override
    public void preStart() {
        final EventStream eventStream = context().system().eventStream();
        final MotorService motorService = MotorServiceFactory.http(getContext().getSystem(), motorNode);

        motors = getContext().actorOf(MotorActor.props(motorService), "motors");

        eventStream.publish(new RegisterSubSystemMessage(self()));
        eventStream.subscribe(self(), KeyEvent.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(KeyEvent.class, this::drive)
                .match(TakeSnapshotMessage.class, this::snapshot)
                .build();
    }

    private void snapshot(TakeSnapshotMessage message) {
        sender().tell(new ObjectSnapshotMessage(message.getUuid(), currentState), self());
    }

    private void drive(final KeyEvent keyEvent) {
        currentState = driverStrategy.drive(keyEvent, currentState);

        log.info("{}", currentState);

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
