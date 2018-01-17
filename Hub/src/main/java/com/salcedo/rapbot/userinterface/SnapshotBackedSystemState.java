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
import com.salcedo.rapbot.sense.ThreeDimensionalSensorReading;
import com.salcedo.rapbot.snapshot.SystemSnapshot;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static java.util.stream.Collectors.joining;


public class SnapshotBackedSystemState implements SystemState {
    private final SystemSnapshot systemSnapshot;
    private final ActorContext context;

    SnapshotBackedSystemState(final SystemSnapshot systemSnapshot, final ActorContext context) {
        this.systemSnapshot = systemSnapshot;
        this.context = context;
    }

    @Override
    public int actualOrientation() {
        return getEnvironmentReading()
                .map(EnvironmentReading::getOrientation)
                .map(Orientation::getYaw)
                .map(Double::intValue)
                .orElse(90);
    }

    private Optional<EnvironmentReading> getEnvironmentReading() {
        final ActorPath sensors = getActorPath("/user/hub/sensors");
        return systemSnapshot.getSnapshot(sensors, EnvironmentReading.class);
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
        return systemSnapshot.getSnapshot(driver, DriveState.class);
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
    public String get3DAcceleration() {
        final Optional<ThreeDimensionalSensorReading> acceleration = getEnvironmentReading()
                .map(EnvironmentReading::getAccelerometer);

        return String.format(
                "{ x: %3.2f, y: %3.2f, z: %3.2f }",
                acceleration.map(ThreeDimensionalSensorReading::getX)
                        .map(this::gravityToCentimeterAcceleration)
                        .orElse(0.0),
                acceleration.map(ThreeDimensionalSensorReading::getY)
                        .map(this::gravityToCentimeterAcceleration)
                        .orElse(0.0),
                acceleration.map(ThreeDimensionalSensorReading::getZ)
                        .map(this::gravityToCentimeterAcceleration)
                        .orElse(0.0)
        );
    }

    private double gravityToCentimeterAcceleration(final double gravity) {
        return 9.81 * gravity * 100;
    }

    @Override
    public int throttle() {
        return getDriveState().map(DriveState::getThrottle).orElse(0);
    }

    @Override
    public String getSnapshotId() {
        String id = systemSnapshot.getUuid().toString();
        return id.substring(id.length() - 8);
    }

    @Override
    public String getSnapshotDuration() {
        final Duration duration = Duration.between(systemSnapshot.getStart(), systemSnapshot.getEnd().orElseGet(Instant::now));
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
        return systemSnapshot.getSnapshot(motor, MotorResponse.class);
    }

    @Override
    public String getRightMotorState() {
        return getMotorState(Location.BACK_RIGHT);
    }

    @Override
    public String getSnapshotSubsystems() {
        return systemSnapshot.getSubsystems().stream()
                .map(ActorPath::name)
                .sorted()
                .collect(joining(", "));
    }

    @Override
    public String getCompletedSnapshotSubsystems() {
        return systemSnapshot.getCompletedSubsystems().stream()
                .map(ActorPath::name)
                .sorted()
                .collect(joining(", "));
    }

    @Override
    public String getImagePath() {
        final ActorPath vision = getActorPath("/user/hub/vision");
        final Optional<Path> path = this.systemSnapshot.getSnapshot(vision, Path.class);

        return path
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .orElse("/dev/null");
    }
}
