package com.salcedo.rapbot.motor;

import java.util.Set;

import static java.util.Collections.unmodifiableSet;

public final class MotorRequest extends Motors {
    MotorRequest(Set<Motor> motors) {
        super(motors);
    }

    public static MotorRequestBuilder builder() {
        return new MotorRequestBuilder();
    }

    public final static class MotorRequestBuilder extends MotorsBuilder {
        public MotorRequest build() {
            return new MotorRequest(unmodifiableSet(getMotors()));
        }

        @Override
        public MotorRequestBuilder addMotor(Motor motor) {
            getMotors().add(motor);
            return this;
        }
    }
}
