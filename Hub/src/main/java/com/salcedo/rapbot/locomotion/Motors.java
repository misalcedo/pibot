package com.salcedo.rapbot.locomotion;

import java.util.LinkedHashSet;
import java.util.Set;

abstract class Motors {
    private final Set<Motor> motors;

    Motors(final Set<Motor> motors) {
        this.motors = motors;
    }

    @Override
    public String toString() {
        return "Motors{" +
                "motors=" + motors +
                '}';
    }

    public static abstract class MotorsBuilder {
        private final Set<Motor> motors = new LinkedHashSet<>();

        public abstract Motors build();

        public abstract MotorsBuilder addMotor(final Motor motor);

        Set<Motor> getMotors() {
            return motors;
        }
    }
}
