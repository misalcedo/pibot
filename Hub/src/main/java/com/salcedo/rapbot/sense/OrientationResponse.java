package com.salcedo.rapbot.sense;

public class OrientationResponse {
    private final double yaw;
    private final double pitch;
    private final double roll;

    public OrientationResponse(final double yaw, final double pitch, final double roll) {
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
}
