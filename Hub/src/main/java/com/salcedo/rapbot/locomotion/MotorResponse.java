package com.salcedo.rapbot.locomotion;

import java.util.HashSet;
import java.util.Set;

public final class MotorResponse extends Motors {
    MotorResponse(Set<Motor> motors) {
        super(motors);
    }

    public static MotorResponseBuilder builder() {
        return new MotorResponseBuilder();
    }

    public final static class MotorResponseBuilder extends MotorsBuilder {
        public MotorResponse build() {
            return new MotorResponse(new HashSet<>(getMotors()));
        }

        @Override
        public MotorResponseBuilder addMotor(Motor motor) {
            getMotors().add(motor);
            return this;
        }
    }
}
