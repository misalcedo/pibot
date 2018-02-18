package com.salcedo.rapbot.hub;

import scala.collection.Set;
import scala.concurrent.duration.Duration;

import java.nio.file.Path;
import java.util.UUID;


public final class SnapshotBackedSystemState implements SystemState {
    private final UUID uuid;
    private final Set<String> subsystems;
    private final Duration duration;
    private final int throttle;
    private final int targetOrientation;
    private final String leftMotorState;
    private final String rightMotorState;
    private final Path image;

    public SnapshotBackedSystemState(
            final UUID uuid,
            final Set<String> subsystems,
            final Duration duration,
            final int throttle,
            final int targetOrientation,
            final String leftMotorState,
            final String rightMotorState,
            final Path image
    ) {
        this.uuid = uuid;
        this.subsystems = subsystems;
        this.duration = duration;
        this.throttle = throttle;
        this.targetOrientation = targetOrientation;
        this.leftMotorState = leftMotorState;
        this.rightMotorState = rightMotorState;
        this.image = image;
    }

    @Override
    public int targetOrientation() {
        return targetOrientation;
    }

    @Override
    public int throttle() {
        return throttle;
    }

    @Override
    public String getSnapshotId() {
        return uuid.toString();
    }

    @Override
    public String getSnapshotDuration() {
        return duration.toString();
    }

    @Override
    public String getLeftMotorState() {
        return leftMotorState;
    }

    @Override
    public String getRightMotorState() {
        return rightMotorState;
    }

    @Override
    public String getCompletedSnapshotSubsystems() {
        return subsystems.toString();
    }

    @Override
    public String getImagePath() {
        return image.toAbsolutePath().toString();
    }
}
