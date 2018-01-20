package com.salcedo.rapbot.locomotion;

import java.util.HashSet;
import java.util.Set;

public final class MotorRequest extends Motors {
    MotorRequest(Set<Motor> motors) {
        super(motors);
    }

    public static MotorRequestBuilder builder() {
        return new MotorRequestBuilder();
    }

    public final static class MotorRequestBuilder extends MotorsBuilder {
        public MotorRequest build() {
            return new MotorRequest(new HashSet<>(getMotors()));
        }

        @Override
        public MotorRequestBuilder addMotor(Motor motor) {
            getMotors().add(motor);
            return this;
        }
    }
}
