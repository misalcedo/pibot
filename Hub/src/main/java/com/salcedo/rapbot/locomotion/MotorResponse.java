package com.salcedo.rapbot.locomotion;

import java.util.Set;

import static java.util.Collections.unmodifiableSet;

public final class MotorResponse extends Motors {
    MotorResponse(Set<Motor> motors) {
        super(motors);
    }

    public static MotorResponseBuilder builder() {
        return new MotorResponseBuilder();
    }

    public final static class MotorResponseBuilder extends MotorsBuilder {
        public MotorResponse build() {
            return new MotorResponse(unmodifiableSet(getMotors()));
        }

        @Override
        public MotorResponseBuilder addMotor(Motor motor) {
            getMotors().add(motor);
            return this;
        }
    }
}
