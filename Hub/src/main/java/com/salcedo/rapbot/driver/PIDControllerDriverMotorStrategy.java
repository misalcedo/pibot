package com.salcedo.rapbot.driver;

import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.locomotion.Command;
import com.salcedo.rapbot.locomotion.Motor;
import com.salcedo.rapbot.locomotion.MotorRequest;
import com.salcedo.rapbot.sense.Orientation;

import static java.lang.Math.PI;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

public class PIDControllerDriverMotorStrategy implements DriverMotorStrategy {
    private static final double EPSILON = 0.0001;
    private final LoggingAdapter log;
    private final PIDController pid;
    private PIDResult pidResult;

    PIDControllerDriverMotorStrategy(final PIDController pid, final ActorSystem system) {
        this.pid = pid;
        this.log = Logging.getLogger(system, this);
        this.pidResult = new PIDResult(0.0);
    }

    @Override
    public MotorRequest drive(final Orientation orientation, final DriveState driveState) {
        final double actual = orientation.getYaw();
        final double target = driveState.getOrientation();
        final double adjustedActual = adjustDegrees(actual, target);
        final double adjustedTarget = adjustDegrees(target, actual);

        pidResult = pid.step(adjustedActual, adjustedTarget, pidResult);

        return controlMotors(pidResult.getOutput(), driveState);
    }

    private double adjustDegrees(final double x, final double y) {
        return isCloseToPi(y - x) ? x + toDegrees(2 * PI) : x;
    }

    private boolean isCloseToPi(final double delta) {
        final double radians = toRadians(delta);
        return radians >= PI + EPSILON || radians >= PI - EPSILON;
    }

    private MotorRequest controlMotors(final double pidOutput, final DriveState driveState) {
        final double rightToLeftRatio = ((pidOutput / toDegrees(2 * PI)) + 1.0) / 2.0;
        final int adjustedLeftThrottle = driveState.getThrottleRange()
                .bounded((int) (driveState.getThrottle() * (1.0 - rightToLeftRatio)));
        final int adjustedRightThrottle = driveState.getThrottleRange()
                .bounded((int) (driveState.getThrottle() * rightToLeftRatio));

        return createMotorRequest(adjustedLeftThrottle, adjustedRightThrottle, driveState.getCommand());
    }

    private MotorRequest createMotorRequest(final int leftSpeed, final int rightSpeed, final Command command) {
        log.debug("Moving motors with command: {}, and speed left: {}, right: {}.", command, leftSpeed, rightSpeed);

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

    public PIDResult getPidResult() {
        return pidResult;
    }
}
