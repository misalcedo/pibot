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

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static java.util.stream.Collectors.joining;


public class SnapshotBackedSystemState implements SystemState {
    private final Snapshot snapshot;
    private final ActorContext context;

    SnapshotBackedSystemState(final Snapshot snapshot, final ActorContext context) {
        this.snapshot = snapshot;
        this.context = context;
    }

    @Override
    public int actualOrientation() {
        return getEnvironmentReading()
                .map(EnvironmentReading::getOrientation)
                .map(Orientation::getYaw)
                .map(Integer.class::cast)
                .orElse(90);
    }

    private Optional<EnvironmentReading> getEnvironmentReading() {
        final ActorPath sensors = getActorPath("/user/hub/sensors");
        return snapshot.getSnapshot(sensors, EnvironmentReading.class);
    }

    private ActorPath getActorPath(final String relativePath) {
        return ActorPaths.fromString("akka://" + context.system().name() + relativePath);
    }

    @Override
    public int targetOrientation() {
        return getDriveState().map(DriveState::getOrientation).orElse(90);
    }

    private Optional<DriveState> getDriveState() {
        final ActorPath driver = getActorPath("/user/hub/driver");
        return snapshot.getSnapshot(driver, DriveState.class);
    }

    @Override
    public String get3DOrientation() {
        final Optional<Orientation> orientation = getEnvironmentReading().map(EnvironmentReading::getOrientation);

        return String.format(
                "{ yaw: %3.2f, pitch: %3.2f, roll: %3.2f }",
                orientation.map(Orientation::getYaw).orElse(0.0),
                orientation.map(Orientation::getPitch).orElse(0.0),
                orientation.map(Orientation::getRoll).orElse(0.0)
        );
    }

    @Override
    public int throttle() {
        return getDriveState().map(DriveState::getThrottle).orElse(0);
    }

    @Override
    public String getSnapshotId() {
        String id = snapshot.getUuid().toString();
        return id.substring(id.length() - 8);
    }

    @Override
    public String getSnapshotDuration() {
        final Duration duration = Duration.between(snapshot.getStart(), snapshot.getEnd().orElseGet(Instant::now));
        return String.valueOf(duration.toMillis());
    }

    @Override
    public String getLeftMotorState() {
        return getMotorState(Location.BACK_LEFT);
    }

    private String getMotorState(final Location location) {
        final Optional<Motor> motor = getMotorResponse().flatMap(location::getMotor);

        return String.format(
                "{ command: %s, speed: %d }",
                motor.map(Motor::getCommand).orElse(Command.RELEASE).name(),
                motor.map(Motor::getSpeed).orElse(0)
        );
    }

    private Optional<MotorResponse> getMotorResponse() {
        final ActorPath motor = getActorPath("/user/hub/motors");
        return snapshot.getSnapshot(motor, MotorResponse.class);
    }

    @Override
    public String getRightMotorState() {
        return getMotorState(Location.BACK_RIGHT);
    }

    @Override
    public String getSnapshotSubsystems() {
        return snapshot.getSubsystems().stream()
                .map(ActorPath::name)
                .sorted()
                .collect(joining(", "));
    }

    @Override
    public String getCompletedSnapshotSubsystems() {
        return snapshot.getCompletedSubsystems().stream()
                .map(ActorPath::name)
                .sorted()
                .collect(joining(", "));
    }

    @Override
    public String getImagePath() {
        final ActorPath vision = getActorPath("/user/hub/vision");
        final Optional<Path> path = this.snapshot.getSnapshot(vision, Path.class);

        return path
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .orElse("/dev/null");
    }
}
