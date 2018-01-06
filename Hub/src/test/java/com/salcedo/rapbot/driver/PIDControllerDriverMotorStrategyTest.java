package com.salcedo.rapbot.driver;

import akka.actor.ActorSystem;
import com.salcedo.rapbot.locomotion.Location;
import com.salcedo.rapbot.locomotion.Motor;
import com.salcedo.rapbot.locomotion.MotorRequest;
import com.salcedo.rapbot.sense.Orientation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.salcedo.rapbot.locomotion.Command.FORWARD;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PIDControllerDriverMotorStrategyTest {
    private ActorSystem system;
    private PIDControllerDriverMotorStrategy motorStrategy;
    @Mock
    private PIDController pidController;

    @Before
    public void setUp() {
        initMocks(this);

        this.system = ActorSystem.create();
        this.motorStrategy = new PIDControllerDriverMotorStrategy(pidController, system);
    }

    @Test
    public void drive() {
        final DriveState driveState = new DriveState(
                new OpenClosedRange(true, true, 0, 255),
                new OpenClosedRange(true, true, 0, 360),
                255,
                15,
                FORWARD
        );
        final Orientation orientation = new Orientation(270.0, 0.0, 0.0);

        when(pidController.step(orientation.getYaw(), 375.0, motorStrategy.getPidResult()))
            .thenReturn(new PIDResult(orientation.getYaw(), 63.0, 0.0));

        final MotorRequest motorRequest = motorStrategy.drive(orientation, driveState);
        final Motor leftMotor = motorRequest.getMotor(Location.BACK_LEFT).orElseThrow(IllegalStateException::new);
        final Motor rightMotor = motorRequest.getMotor(Location.BACK_RIGHT).orElseThrow(IllegalStateException::new);

        assertThat(leftMotor.getCommand(), is(equalTo(rightMotor.getCommand())));
        assertThat(driveState.getCommand(), is(equalTo(rightMotor.getCommand())));
        assertThat(rightMotor.getSpeed(), is(greaterThan(leftMotor.getSpeed())));
    }
}