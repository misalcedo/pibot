package com.salcedo.rapbot.userinterface;

import akka.actor.ActorContext;
import akka.actor.ActorPath;
import akka.actor.ActorPaths;
import com.salcedo.rapbot.driver.DriveState;
import com.salcedo.rapbot.locomotion.Command;
import com.salcedo.rapbot.locomotion.Location;
import com.salcedo.rapbot.locomotion.Motor;
import com.salcedo.rapbot.locomotion.MotorResponse;
import com.salcedo.rapbot.sense.EnvironmentReading;
import com.salcedo.rapbot.sense.Orientation;
import com.salcedo.rapbot.snapshot.Snapshot;

import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Optional;


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
        return (int) getEnvironmentReading().getRelativeOrientation().getYaw();
    }

    private EnvironmentReading getEnvironmentReading() {
        final ActorPath driver = getActorPath("/user/hub/sensors");
        return snapshot.getSnapshot(driver, EnvironmentReading.class);
    }

    private ActorPath getActorPath(final String relativePath) {
        return ActorPaths.fromString("akka://" + context.system().name() + relativePath);
    }

    @Override
    public int targetOrientation() {
        return getDriveState().getOrientation();
    }

    private DriveState getDriveState() {
        final ActorPath driver = getActorPath("/user/hub/driver");
        return snapshot.getSnapshot(driver, DriveState.class);
    }

    @Override
    public String get3DOrientation() {
        final Orientation orientation = getEnvironmentReading().getOrientation();
        return String.format(
                "{ yaw: %3.2f, pitch: %3.2f, roll: %3.2f}",
                orientation.getYaw(),
                orientation.getPitch(),
                orientation.getRoll()
        );
    }

    @Override
    public int throttle() {
        return getDriveState().getThrottle();
    }

    @Override
    public int leftSpeed() {
        return getMotorState().getMotor(Location.BACK_LEFT)
            .map(Motor::getSpeed)
            .orElse(0);
    }

    private MotorResponse getMotorState() {
        final ActorPath motor = getActorPath("/user/hub/motors");
        return snapshot.getSnapshot(motor, MotorResponse.class);
    }

    @Override
    public int rightSpeed() {
        return getMotorState().getMotor(Location.BACK_RIGHT)
                .map(Motor::getSpeed)
                .orElse(0);
    }

    @Override
    public String leftCommand() {
        return getMotorState().getMotor(Location.BACK_LEFT)
                .map(Motor::getCommand)
                .orElse(Command.RELEASE)
                .name();
    }

    @Override
    public String rightCommand() {
        return getMotorState().getMotor(Location.BACK_RIGHT)
                .map(Motor::getCommand)
                .orElse(Command.RELEASE)
                .name();
    }

    @Override
    public String getSnapshotId() {
        String id = snapshot.getUuid().toString();
        return id.substring(id.length() - 8);
    }

    @Override
    public String getSnapshotDuration() {
        final Duration duration = Duration.between(snapshot.getStart(), snapshot.getEnd());
        return String.valueOf(duration.toMillis());
    }
}
