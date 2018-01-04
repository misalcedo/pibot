package com.salcedo.rapbot.driver;

import com.salcedo.rapbot.locomotion.Command;

import static com.salcedo.rapbot.locomotion.Command.BACKWARD;
import static com.salcedo.rapbot.locomotion.Command.FORWARD;

public final class DriveState {
    private final Range throttleRange;
    private final Range orientationRange;
    private final int throttle;
    private final int orientation;
    private final Command command;

    DriveState(
            final Range throttleRange,
            final Range orientationRange,
            final int throttle,
            final int orientation,
            final Command command
    ) {
        this.throttleRange = throttleRange;
        this.orientationRange = orientationRange;
        this.command = command;
        this.throttle = throttleRange.bounded(throttle);
        this.orientation = orientationRange.modulo(orientation);
    }

    public DriveState updateThrottle(int delta) {
        return new DriveState(throttleRange, orientationRange, throttle + delta, orientation, command);
    }

    public DriveState maxThrottle() {
        return new DriveState(throttleRange, orientationRange, throttleRange.last(), orientation, command);
    }

    public DriveState minThrottle() {
        return new DriveState(throttleRange, orientationRange, throttleRange.first(), orientation, command);
    }

    public DriveState updateOrientation(int delta) {
        return new DriveState(throttleRange, orientationRange, throttle, orientation + delta, command);
    }

    public DriveState toggleCommand() {
        return new DriveState(
                throttleRange,
                orientationRange,
                throttle,
                orientation - (orientationRange.distance() / 2),
                reverseCommand()
        );
    }

    private Command reverseCommand() {
        switch (command) {
            case FORWARD:
                return BACKWARD;
            case BACKWARD:
                return FORWARD;
            default:
                return command;
        }
    }

    public int getThrottle() {
        return throttle;
    }

    public int getOrientation() {
        return orientation;
    }

    public Range getThrottleRange() {
        return throttleRange;
    }

    public Range getOrientationRange() {
        return orientationRange;
    }

    public Command getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return "DriveState{" +
                "throttle=" + throttle +
                ", orientation=" + orientation +
                '}';
    }
}
