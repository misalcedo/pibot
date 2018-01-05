package com.salcedo.rapbot.userinterface;

public interface SystemState {
    int actualOrientation();
    int targetOrientation();
    String get3DOrientation();
    int throttle();

    int leftSpeed();
    int rightSpeed();
    String  leftCommand();
    String rightCommand();

    String getSnapshotId();
    String getSnapshotDuration();
}
