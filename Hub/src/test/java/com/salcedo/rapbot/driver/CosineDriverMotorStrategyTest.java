package com.salcedo.rapbot.driver;

import com.salcedo.rapbot.locomotion.Command;
import com.salcedo.rapbot.locomotion.Location;
import com.salcedo.rapbot.locomotion.Motor;
import com.salcedo.rapbot.locomotion.MotorRequest;
import org.junit.Before;
import org.junit.Test;

import static com.salcedo.rapbot.locomotion.Command.BACKWARD;
import static com.salcedo.rapbot.locomotion.Command.FORWARD;
import static com.salcedo.rapbot.locomotion.Location.BACK_LEFT;
import static com.salcedo.rapbot.locomotion.Location.BACK_RIGHT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CosineDriverMotorStrategyTest {
    private CosineDriverMotorStrategy motorStrategy;

    @Before
    public void setUp() {
        this.motorStrategy = new CosineDriverMotorStrategy();
    }

    @Test
    public void driveForwardLeft() {
        assertTurningDrive(120, FORWARD, BACK_RIGHT, BACK_LEFT);
    }

    private void assertTurningDrive(
            final int orientation,
            final Command command,
            final Location fasterLocation,
            final Location slowerLocation
    ) {
        final DriveState driveState = createDriveState(orientation);
        final MotorRequest motorRequest = motorStrategy.drive(driveState);
        final Motor slowerMotor = motorRequest.getMotor(slowerLocation).orElseThrow(IllegalStateException::new);
        final Motor fasterMotor = motorRequest.getMotor(fasterLocation).orElseThrow(IllegalStateException::new);

        assertThat(slowerMotor.getCommand(), is(equalTo(fasterMotor.getCommand())));
        assertThat(slowerMotor.getCommand(), is(equalTo(command)));
        assertThat(fasterMotor.getSpeed(), is(greaterThan(slowerMotor.getSpeed())));
        assertThat(slowerMotor.getSpeed(), is(greaterThan(0)));
    }

    private DriveState createDriveState(final int orientation) {
        return new DriveState(
                new OpenClosedRange(0, 255),
                new OpenClosedRange(0, 360),
                255,
                orientation
        );
    }

    @Test
    public void driveBackwardLeft() {
        assertTurningDrive(210, BACKWARD, BACK_LEFT, BACK_RIGHT);
    }

    @Test
    public void driveForwardRight() {
        assertTurningDrive(30, FORWARD, BACK_LEFT, BACK_RIGHT);
    }

    @Test
    public void driveBackwardRight() {
        assertTurningDrive(300, BACKWARD, BACK_RIGHT, BACK_LEFT);
    }

    @Test
    public void driveForward() {
        assertStraightDrive(90, FORWARD);
    }

    private void assertStraightDrive(final int orientation, final Command command) {
        final DriveState driveState = createDriveState(orientation);
        final MotorRequest motorRequest = motorStrategy.drive(driveState);
        final Motor leftMotor = motorRequest.getMotor(BACK_LEFT).orElseThrow(IllegalStateException::new);
        final Motor rightMotor = motorRequest.getMotor(BACK_RIGHT).orElseThrow(IllegalStateException::new);

        assertThat(leftMotor.getCommand(), is(equalTo(rightMotor.getCommand())));
        assertThat(leftMotor.getCommand(), is(equalTo(command)));
        assertThat(rightMotor.getSpeed(), is(equalTo(leftMotor.getSpeed())));
        assertThat(leftMotor.getSpeed(), is(equalTo(driveState.getThrottle())));
    }

    @Test
    public void driveBackward() {
        assertStraightDrive(270, BACKWARD);
    }

    @Test
    public void driveLeft() {
        assertSidewaysDrive(180, BACK_LEFT, BACK_RIGHT);
    }

    private void assertSidewaysDrive(
            final int orientation,
            final Location zeroLocation,
            final Location fullLocation
    ) {
        final DriveState driveState = createDriveState(orientation);
        final MotorRequest motorRequest = motorStrategy.drive(driveState);
        final Motor fullMotor = motorRequest.getMotor(fullLocation).orElseThrow(IllegalStateException::new);
        final Motor zeroMotor = motorRequest.getMotor(zeroLocation).orElseThrow(IllegalStateException::new);

        assertThat(fullMotor.getCommand(), is(equalTo(zeroMotor.getCommand())));
        assertThat(fullMotor.getCommand(), is(equalTo(FORWARD)));
        assertThat(zeroMotor.getSpeed(), is(equalTo(0)));
        assertThat(fullMotor.getSpeed(), is(equalTo(driveState.getThrottle())));
    }

    @Test
    public void driveRight() {
        assertSidewaysDrive(0, BACK_RIGHT, BACK_LEFT);
        assertSidewaysDrive(360, BACK_RIGHT, BACK_LEFT);
    }
}