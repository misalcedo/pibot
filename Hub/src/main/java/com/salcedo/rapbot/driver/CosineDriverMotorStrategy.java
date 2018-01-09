package com.salcedo.rapbot.driver;

import com.salcedo.rapbot.locomotion.Command;
import com.salcedo.rapbot.locomotion.Motor;
import com.salcedo.rapbot.locomotion.MotorRequest;

import static java.lang.Math.*;

public class CosineDriverMotorStrategy implements DriverMotorStrategy {
    @Override
    public MotorRequest drive(final DriveState driveState) {
        final double orientation = toRadians(driveState.getOrientation());
        final int throttle = driveState.getThrottle();
        final int adjustedThrottle = (int) abs(floor(sin(orientation) * throttle));
        final Command command = (orientation - PI) > 0 ? Command.BACKWARD : Command.FORWARD;

        final int leftSpeed = shouldAdjustRightMotor(orientation) ? throttle : adjustedThrottle;
        final int rightSpeed = shouldAdjustRightMotor(orientation) ? adjustedThrottle : throttle;

        return createMotorRequest(leftSpeed, rightSpeed, command);
    }

    private boolean shouldAdjustRightMotor(final double orientation) {
        final double quadrant = orientation / (PI / 2);
        final boolean inFirstQuadrant = quadrant >= 0.0 && quadrant < 1.0;
        final boolean inThirdQuadrant = quadrant > 2.0 && quadrant <= 3.0;

        return inFirstQuadrant || inThirdQuadrant;
    }

    private MotorRequest createMotorRequest(final int leftSpeed, final int rightSpeed, final Command command) {
        final Motor backLeftMotor = Motor.builder()
                .withCommand(command)
                .withBackLeftLocation()
                .withSpeed(leftSpeed)
                .build();
        final Motor backRightMotor = Motor.builder()
                .withCommand(command)
                .withBackRightLocation()
                .withSpeed(rightSpeed)
                .build();

        return MotorRequest.builder()
                .addMotor(backLeftMotor)
                .addMotor(backRightMotor)
                .build();
    }
}
