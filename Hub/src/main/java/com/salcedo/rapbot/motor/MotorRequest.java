package com.salcedo.rapbot.motor;

import java.util.HashMap;
import java.util.Map;

public final class MotorRequest {
    private final Map<Location, Motor> motors;

    private MotorRequest(Map<Location, Motor> motors) {
        this.motors = motors;
    }

    public static MotorRequestBuilder builder() {
        return new MotorRequestBuilder();
    }

    public static class MotorRequestBuilder {
        private final Map<Location, Motor> motors = new HashMap<>();

        public MotorRequest build() {
            return new MotorRequest(motors);
        }

        public MotorRequestBuilder addMotor(Motor motor) {
            motors.put(motor.getLocation(), motor);
            return this;
        }
    }

    @Override
    public String toString() {
        return "MotorRequest{" +
                "motors=" + motors +
                '}';
    }
}
