package com.salcedo.rapbot.userinterface;

public interface SystemState {
    int targetOrientation();
    int throttle();

    String getSnapshotId();
    String getSnapshotEnd();
    String getSnapshotStart();
}
