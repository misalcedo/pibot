package com.salcedo.rapbot.vehicle;

public class DriveMessage {
    private final Angle angle;
    private final Throttle throttle;

    public DriveMessage(final Angle angle, final Throttle throttle) {
        this.angle = angle;
        this.throttle = throttle;
    }

    public Angle getAngle() {
        return angle;
    }

    public Throttle getThrottle() {
        return throttle;
    }
}
