package com.salcedo.rapbot.userinterface;

import akka.actor.ActorContext;
import akka.actor.ActorPath;
import akka.actor.ActorPaths;
import com.salcedo.rapbot.driver.DriveState;
import com.salcedo.rapbot.sense.EnvironmentReading;
import com.salcedo.rapbot.snapshot.Snapshot;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;


public class SnapshotBackedSystemState implements SystemState {
    private final Snapshot snapshot;
    private final ActorContext context;
    private final DateTimeFormatter dateTimeFormatter;

    public SnapshotBackedSystemState(final Snapshot snapshot, final ActorContext context) {
        this.snapshot = snapshot;
        this.context = context;
        this.dateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)
                .withLocale(Locale.US)
                .withZone(ZoneId.systemDefault());
    }

    @Override
    public int actualOrientation() {
        return (int) getEnvironmentReading().getOrientation().getYaw();
    }

    private EnvironmentReading getEnvironmentReading() {
        final ActorPath driver = getActorPath("/user/hub/sensors");
        return snapshot.getSnapshot(driver, EnvironmentReading.class);
    }

    @Override
    public int targetOrientation() {
        return getDriveState().getOrientation();
    }

    private DriveState getDriveState() {
        final ActorPath driver = getActorPath("/user/hub/driver");
        return snapshot.getSnapshot(driver, DriveState.class);
    }

    private ActorPath getActorPath(final String relativePath) {
        return ActorPaths.fromString("akka://" + context.system().name() + relativePath);
    }

    @Override
    public int throttle() {
        return getDriveState().getThrottle();
    }

    @Override
    public String getSnapshotId() {
        String id = snapshot.getUuid().toString();
        return id.substring(id.length() - 8);
    }

    @Override
    public String getSnapshotEnd() {
        return dateTimeFormatter.format(snapshot.getEnd());
    }

    @Override
    public String getSnapshotStart() {
        return dateTimeFormatter.format(snapshot.getStart());
    }
}
