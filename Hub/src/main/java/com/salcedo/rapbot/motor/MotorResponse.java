package com.salcedo.rapbot.motor;

import java.util.HashMap;
import java.util.Map;

public final class MotorResponse {
    private final Map<Location, Motor> motors;

    private MotorResponse(Map<Location, Motor> motors) {
        this.motors = motors;
    }

    public static MotorResponseBuilder builder() {
        return new MotorResponseBuilder();
    }

    public static class MotorResponseBuilder {
        private final Map<Location, Motor> motors = new HashMap<>();

        public MotorResponse build() {
            return new MotorResponse(motors);
        }

        public MotorResponseBuilder addMotor(Motor motor) {
            motors.put(motor.getLocation(), motor);
            return this;
        }
    }

    @Override
    public String toString() {
        return "MotorResponse{" +
                "motors=" + motors +
                '}';
    }
}
