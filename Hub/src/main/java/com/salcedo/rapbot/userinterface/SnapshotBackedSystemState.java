package com.salcedo.rapbot.userinterface;

import akka.actor.ActorContext;
import akka.actor.ActorPath;
import akka.actor.ActorPaths;
import com.salcedo.rapbot.driver.DriveState;
import com.salcedo.rapbot.sense.EnvironmentReading;
import com.salcedo.rapbot.snapshot.Snapshot;


public class SnapshotBackedSystemState implements SystemState {
    private final Snapshot snapshot;
    private final ActorContext context;

    public SnapshotBackedSystemState(final Snapshot snapshot, final ActorContext context) {
        this.snapshot = snapshot;
        this.context = context;
    }

    @Override
    public double getTemperature() {
        final ActorPath senses = getActorPath("/user/hub/senses");
        final EnvironmentReading environmentReading = this.snapshot.getSnapshot(senses, EnvironmentReading.class);
        return environmentReading.getTemperature();
    }

    @Override
    public DriveState getDriveState() {
        final ActorPath driver = getActorPath("/user/hub/driver");
        return snapshot.getSnapshot(driver, DriveState.class);
    }

    private ActorPath getActorPath(final String relativePath) {
        return ActorPaths.fromString("akka://" + context.system().name() + relativePath);
    }
}
