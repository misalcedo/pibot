package com.salcedo.rapbot.hub.com.salcedo.rapbot.hub.driver.random;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.hub.com.salcedo.rapbot.hub.driver.invoke.DriveRequest;
import com.salcedo.rapbot.hub.com.salcedo.rapbot.hub.driver.invoke.DriveResponse;
import com.salcedo.rapbot.motor.Motor;
import com.salcedo.rapbot.motor.MotorRequest;
import com.salcedo.rapbot.motor.MotorService;

import java.security.SecureRandom;
import java.util.Random;

public final class RandomDriver extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Random random;
    private final MotorService motorService;

    private RandomDriver(MotorService motorService) {
        this.motorService = motorService;
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

        ActorRef sender = getSender();
        MotorRequest request = createRequest(command, speed);
        motorService.drive(request)
                .thenAccept(motorResponse -> respondToDriveRequest(sender));
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

    private void respondToDriveRequest(ActorRef sender) {
        sender.tell(new DriveResponse(), self());
    }
}
