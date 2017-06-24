package com.salcedo.rapbot.hub.com.salcedo.rapbot.hub.driver;

public final class DriveResponse {
    private final String hostname;
    private final String response;

    public DriveResponse(String hostname, String response) {
        this.hostname = hostname;
        this.response = response;
    }

    public String getHostname() {
        return hostname;
    }

    public String getResponse() {
        return response;
    }
}
