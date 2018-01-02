package com.salcedo.rapbot.driver;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.locomotion.Motor;
import com.salcedo.rapbot.locomotion.MotorRequest;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public final class RandomDriver extends AbstractActor {
    private static final FiniteDuration DRIVE_DELAY = Duration.create(1L, TimeUnit.SECONDS);
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Random random;
    private final ActorRef motors;

    private RandomDriver(final ActorRef motors) {
        this.motors = motors;
        this.random = new SecureRandom();
    }

    @Override
    public void preStart() throws Exception {
        getContext()
                .getSystem()
                .scheduler()
                .scheduleOnce(
                        DRIVE_DELAY,
                        self(),
                        new DriveState(),
                        getContext().dispatcher(),
                        self()
                );
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DriveState.class, request -> drive())
                .build();
    }

    private void drive() {
        final int speed = random.nextInt(256);
        final int command = random.nextInt(2) + 1;

        log.info("Fulfilling a drive request with speed {} and command {}", speed, command);

        motors.forward(createRequest(command, speed), getContext());
    }

    private Motor withCommand(final int command, final Motor.MotorBuilder motorBuilder) {
        switch (command) {
            case 1:
                return motorBuilder.withForwardCommand().build();
            case 2:
                return motorBuilder.withBackwardCommand().build();
            default:
                return motorBuilder.withReleaseCommand().build();
        }
    }

    private MotorRequest createRequest(final int command, final int speed) {
        final Motor backLeftMotor = withCommand(command, Motor.builder()
                .withBackLeftLocation()
                .withSpeed(speed));
        final Motor backRightMotor = withCommand(command, Motor.builder()
                .withBackRightLocation()
                .withSpeed(speed));

        return MotorRequest.builder()
                .addMotor(backLeftMotor)
                .addMotor(backRightMotor)
                .build();
    }
}
