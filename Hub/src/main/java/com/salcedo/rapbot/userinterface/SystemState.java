package com.salcedo.rapbot.userinterface;

public interface SystemState {
    int actualOrientation();
    int targetOrientation();
    String get3DOrientation();
    int throttle();

    String getSnapshotId();
    String getSnapshotDuration();

    String getLeftMotorState();
    String getRightMotorState();

    String getSnapshotSubsystems();
    String getCompletedSnapshotSubsystems();

    String getImagePath();

    String get3DAcceleration();
}
