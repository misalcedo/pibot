package com.salcedo.rapbot.sense;

public class OrientationRequest {
    private final boolean isRelative;

    public OrientationRequest(boolean isRelative) {
        this.isRelative = isRelative;
    }

    public boolean isRelative() {
        return isRelative;
    }
}
