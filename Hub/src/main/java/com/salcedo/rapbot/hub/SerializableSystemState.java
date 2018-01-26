package com.salcedo.rapbot.hub;

public final class SerializableSystemState implements SystemState {
    private final int targetOrientation;
    private final int throttle;
    private final String snapshotDuration;
    private final String snapshotId;

    SerializableSystemState(final SystemState systemState) {
        this.snapshotId = systemState.getSnapshotId();
        this.snapshotDuration = systemState.getSnapshotDuration();
        this.throttle = systemState.throttle();
        this.targetOrientation = systemState.targetOrientation();
    }

    @Override
    public int actualOrientation() {
        return 0;
    }

    @Override
    public int targetOrientation() {
        return targetOrientation;
    }

    @Override
    public String get3DOrientation() {
        return null;
    }

    @Override
    public int throttle() {
        return throttle;
    }

    @Override
    public String getSnapshotId() {
        return snapshotId;
    }

    @Override
    public String getSnapshotDuration() {
        return snapshotDuration;
    }

    @Override
    public String getLeftMotorState() {
        return null;
    }

    @Override
    public String getRightMotorState() {
        return null;
    }

    @Override
    public String getSnapshotSubsystems() {
        return null;
    }

    @Override
    public String getCompletedSnapshotSubsystems() {
        return null;
    }

    @Override
    public String getImagePath() {
        return null;
    }

    @Override
    public String get3DAcceleration() {
        return null;
    }
}
