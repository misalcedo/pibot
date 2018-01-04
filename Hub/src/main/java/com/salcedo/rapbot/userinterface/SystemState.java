package com.salcedo.rapbot.userinterface;

public interface SystemState {
    int actualOrientation();
    int targetOrientation();
    int throttle();

    String getSnapshotId();
    String getSnapshotEnd();
    String getSnapshotStart();
}
