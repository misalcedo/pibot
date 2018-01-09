package com.salcedo.rapbot.driver;

public final class DriveState {
    private final Range throttleRange;
    private final Range orientationRange;
    private final int throttle;
    private final int orientation;

    DriveState(
            final Range throttleRange,
            final Range orientationRange,
            final int throttle,
            final int orientation
    ) {
        this.throttleRange = throttleRange;
        this.orientationRange = orientationRange;
        this.throttle = throttleRange.bounded(throttle);
        this.orientation = orientationRange.modulo(orientation);
    }

    public DriveState updateThrottle(int delta) {
        return new DriveState(throttleRange, orientationRange, throttle + delta, orientation);
    }

    public DriveState maxThrottle() {
        return new DriveState(throttleRange, orientationRange, throttleRange.last(), orientation);
    }

    public DriveState minThrottle() {
        return new DriveState(throttleRange, orientationRange, throttleRange.first(), orientation);
    }

    public DriveState updateOrientation(int delta) {
        return new DriveState(throttleRange, orientationRange, throttle, orientation + delta);
    }

    public DriveState withOrientation(int newOrientation) {
        return new DriveState(throttleRange, orientationRange, throttle, newOrientation);
    }

    public int getThrottle() {
        return throttle;
    }

    public int getOrientation() {
        return orientation;
    }

    @Override
    public String toString() {
        return "DriveState{" +
                "throttle=" + throttle +
                ", orientation=" + orientation +
                '}';
    }
}
