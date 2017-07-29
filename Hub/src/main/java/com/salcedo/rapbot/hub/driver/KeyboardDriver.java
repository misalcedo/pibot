package com.salcedo.rapbot.hub.driver;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.motor.Motor;
import com.salcedo.rapbot.motor.MotorRequest;

import java.awt.event.KeyEvent;

public final class KeyboardDriver extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final ActorRef motors;

    private KeyboardDriver(final ActorRef motors) {
        this.motors = motors;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(KeyEvent.class, this::drive)
                .build();
    }

    private void drive(KeyEvent keyEvent) {
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
        }

        log.info("Received a key event. Event: {}", keyEvent);
    }

    private MotorRequest createStopRequest() {
        Motor backLeftMotor = Motor.builder()
                .withReleaseCommand()
                .withBackLeftLocation()
                .withSpeed(0)
                .build();
        Motor backRightMotor = Motor.builder()
                .withReleaseCommand()
                .withBackRightLocation()
                .withSpeed(0)
                .build();

        return createMotorRequest(backLeftMotor, backRightMotor);
    }

    private MotorRequest createMotorRequest(Motor backLeftMotor, Motor backRightMotor) {
        return MotorRequest.builder()
                .addMotor(backLeftMotor)
                .addMotor(backRightMotor)
                .build();
    }

    private MotorRequest createForwardRequest(int leftSpeed, int rightSpeed) {
        Motor backLeftMotor = Motor.builder()
                .withForwardCommand()
                .withBackLeftLocation()
                .withSpeed(leftSpeed)
                .build();
        Motor backRightMotor = Motor.builder()
                .withForwardCommand()
                .withBackRightLocation()
                .withSpeed(rightSpeed)
                .build();

        return createMotorRequest(backLeftMotor, backRightMotor);
    }

    private MotorRequest createReverseRequest() {
        Motor backLeftMotor = Motor.builder()
                .withBackwardCommand()
                .withBackLeftLocation()
                .withSpeed(255)
                .build();
        Motor backRightMotor = Motor.builder()
                .withBackwardCommand()
                .withBackRightLocation()
                .withSpeed(255)
                .build();

        return createMotorRequest(backLeftMotor, backRightMotor);
    }
}
