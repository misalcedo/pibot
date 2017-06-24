package com.salcedo.rapbot.hub.com.salcedo.rapbot.hub.driver;

public final class DriveRequest {
    private final String hostname;

    public DriveRequest(String hostname) {
        this.hostname = hostname;
    }

    String getHostname() {
        return hostname;
    }
}
