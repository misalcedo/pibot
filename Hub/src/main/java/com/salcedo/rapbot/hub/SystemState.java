package com.salcedo.rapbot.hub;

public interface SystemState {
    int targetOrientation();
    int throttle();

    String getSnapshotId();
    String getSnapshotDuration();

    String getLeftMotorState();
    String getRightMotorState();

    String getCompletedSnapshotSubsystems();

    String getImagePath();
}
