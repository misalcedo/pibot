package com.salcedo.rapbot.hub.driver;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.motor.Motor;
import com.salcedo.rapbot.motor.MotorRequest;

import java.security.SecureRandom;
import java.util.Random;

public final class RandomDriver extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Random random;
    private final ActorRef motors;

    private RandomDriver(final ActorRef motors) {
        this.motors = motors;
        this.random = new SecureRandom();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DriveRequest.class, request -> drive())
                .build();
    }

    private void drive() {
        final int speed = random.nextInt(256);
        final int command = random.nextInt(2) + 1;

        log.info("Fulfilling a drive request with speed {} and command {}", speed, command);

        motors.forward(createRequest(command, speed), getContext());
    }

    private Motor withCommand(int command, Motor.MotorBuilder motorBuilder) {
        switch (command) {
            case 1:
                return motorBuilder.withForwardCommand().build();
            case 2:
                return motorBuilder.withBackwardCommand().build();
            default:
                return motorBuilder.withReleaseCommand().build();
        }
    }

    private MotorRequest createRequest(int command, int speed) {
        Motor backLeftMotor = withCommand(command, Motor.builder()
                .withBackLeftLocation()
                .withSpeed(speed));
        Motor backRightMotor = withCommand(command, Motor.builder()
                .withBackRightLocation()
                .withSpeed(speed));

        return MotorRequest.builder()
                .addMotor(backLeftMotor)
                .addMotor(backRightMotor)
                .build();
    }
}
