package com.salcedo.rapbot.sense;

public class Orientation {
    private final double yaw;
    private final double pitch;
    private final double roll;

    public Orientation(final double yaw, final double pitch, final double roll) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }

    public double getYaw() {
        return yaw;
    }

    public double getPitch() {
        return pitch;
    }

    public double getRoll() {
        return roll;
    }

    @Override
    public String toString() {
        return "Orientation{" +
                "yaw=" + yaw +
                ", pitch=" + pitch +
                ", roll=" + roll +
                '}';
    }
}
