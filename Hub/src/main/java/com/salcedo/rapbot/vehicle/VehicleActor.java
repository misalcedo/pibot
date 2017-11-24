package com.salcedo.rapbot.vehicle;

import akka.actor.AbstractActor;
import com.salcedo.rapbot.motor.Motor;
import com.salcedo.rapbot.motor.MotorRequest;
import com.salcedo.rapbot.motor.MotorService;

public final class VehicleActor extends AbstractActor {
    private final MotorService motorService;

    public VehicleActor(final MotorService motorService) {
        this.motorService = motorService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DriveMessage.class, this::drive)
                .match(BrakeMessage.class, this::brake)
                .build();
    }

    private void brake(final BrakeMessage brakeMessage) {
        final Motor backLeftMotor = Motor.builder()
                .withBackLeftLocation()
                .withBrakeCommand()
                .withSpeed(0)
                .build();

        final Motor backRightMotor = Motor.builder()
                .withBackRightLocation()
                .withBrakeCommand()
                .withSpeed(0)
                .build();

        motorService.drive(MotorRequest.builder()
                .addMotor(backLeftMotor)
                .addMotor(backRightMotor)
                .build());
    }

    private void drive(final DriveMessage driveMessage) {
        motorService.drive(createMotorRequest(driveMessage));
    }

    private MotorRequest createMotorRequest(final DriveMessage driveMessage) {
        final Motor backLeftMotor = Motor.builder()
                .withBackLeftLocation()
                .withSpeed(0)
                .build();

        final Motor backRightMotor = Motor.builder()
                .withBackRightLocation()
                .withSpeed(0)
                .build();
        return MotorRequest.builder()
                .addMotor(backLeftMotor)
                .addMotor(backRightMotor)
                .build();
    }
}
