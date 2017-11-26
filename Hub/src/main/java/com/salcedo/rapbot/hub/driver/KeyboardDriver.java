package com.salcedo.rapbot.hub.driver;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.model.Uri;
import com.salcedo.rapbot.motor.Motor;
import com.salcedo.rapbot.motor.MotorActor;
import com.salcedo.rapbot.motor.MotorRequest;
import com.salcedo.rapbot.motor.MotorServiceFactory;
import com.salcedo.rapbot.snapshot.StartSnapshotMessage;

import java.awt.event.KeyEvent;

public final class KeyboardDriver extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Uri motorNode;
    private ActorRef motors;

    public KeyboardDriver(final Uri motorNode) {
        this.motorNode = motorNode;
    }

    @Override
    public void preStart() throws Exception {
        motors = getContext().actorOf(Props.create(
                MotorActor.class,
                MotorServiceFactory.http(getContext().getSystem(), motorNode)
        ));

        context().system().eventStream().subscribe(self(), KeyEvent.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(KeyEvent.class, this::drive)
                .build();
    }

    private void drive(final KeyEvent keyEvent) {
        if (keyEvent.getID() != KeyEvent.KEY_RELEASED) {
            return;
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_UP) {
            motors.forward(createForwardRequest(255, 255), getContext());
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN) {
            motors.forward(createReverseRequest(), getContext());
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_RIGHT) {
            motors.forward(createForwardRequest(255, 127), getContext());
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_LEFT) {
            motors.forward(createForwardRequest(127, 255), getContext());
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_SPACE) {
            motors.forward(createStopRequest(), getContext());
        } else {
            log.info("Received a key event. Event: {}", keyEvent);
            return;
        }

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
