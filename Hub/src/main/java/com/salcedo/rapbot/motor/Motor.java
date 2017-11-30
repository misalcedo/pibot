package com.salcedo.rapbot.motor;

import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;

/**
 * MotorActor are used to move the robot.
 */
public final class Motor {
    private final Location location;
    private final Command command;
    private final int speed;

    private Motor(final Location location, final Command command, final int speed) {
        this.location = location;
        this.command = command;
        this.speed = speed;
    }

    public static MotorBuilder builder() {
        return new MotorBuilder();
    }

    Location getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "Motor{" +
                "location=" + location +
                ", command=" + command +
                ", speed=" + speed +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        return object != null && (this == object || ((object instanceof Motor) && equals((Motor) object)));
    }

    private boolean equals(Motor motor) {
        return location == motor.location && command == motor.command && speed == motor.speed;
    }

    @Override
    public int hashCode() {
        return hash(location, command, speed);
    }

    public static class MotorBuilder {
        private Location location;
        private Command command;
        private int speed;

        public Motor build() {
            return new Motor(requireNonNull(location), requireNonNull(command), requireNonNull(speed));
        }

        public MotorBuilder withBackLeftLocation() {
            location = Location.BACK_LEFT;
            return this;
        }

        public MotorBuilder withBackRightLocation() {
            location = Location.BACK_RIGHT;
            return this;
        }

        public MotorBuilder withForwardCommand() {
            command = Command.FORWARD;
            return this;
        }

        public MotorBuilder withBackwardCommand() {
            command = Command.BACKWARD;
            return this;
        }

        public MotorBuilder withReleaseCommand() {
            command = Command.RELEASE;
            return this;
        }

        public MotorBuilder withBrakeCommand() {
            command = Command.BRAKE;
            return this;
        }

        public MotorBuilder withSpeed(final int speed) {
            this.speed = validateSpeed(speed);
            return this;
        }

        private int validateSpeed(final int speed) {
            if (speed > 255 || speed < 0) {
                throw new IllegalArgumentException(
                        String.format("Invalid speed. Speed must be 0 <= speed <= 255. (Speed = %s)", speed)
                );
            }

            return speed;
        }
    }
}
