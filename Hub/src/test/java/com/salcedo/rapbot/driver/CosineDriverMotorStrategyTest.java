package com.salcedo.rapbot.driver;

import akka.actor.ActorSystem;
import com.salcedo.rapbot.locomotion.Command;
import com.salcedo.rapbot.locomotion.Location;
import com.salcedo.rapbot.locomotion.Motor;
import com.salcedo.rapbot.locomotion.MotorRequest;
import org.junit.Before;
import org.junit.Test;

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
    public void drive() {
        final DriveState driveState = new DriveState(
                new OpenClosedRange(true, true, 0, 255),
                new OpenClosedRange(true, true, 0, 360),
                255,
                15
        );
        final MotorRequest motorRequest = motorStrategy.drive(driveState);
        final Motor leftMotor = motorRequest.getMotor(Location.BACK_LEFT).orElseThrow(IllegalStateException::new);
        final Motor rightMotor = motorRequest.getMotor(Location.BACK_RIGHT).orElseThrow(IllegalStateException::new);

        assertThat(leftMotor.getCommand(), is(equalTo(rightMotor.getCommand())));
        assertThat(rightMotor.getSpeed(), is(greaterThan(leftMotor.getSpeed())));
    }

    @Test
    public void driveV2() {
        final int throttle = 255;

        System.out.println("Degree\tOpp\tLeft\tRight\tCommand");
        for (int i = 0; i <= 180; i += 15) {
            final MotorRequest motorRequest = motorStrategy.drive(createDriveState(255, i));

            System.out.printf(
                    "%d\t%f\t%d\t%d\t%s\n",
                    i,
                    Math.sin(Math.toRadians(i)) * throttle,
                    motorRequest
                            .getMotor(Location.BACK_LEFT)
                            .map(Motor::getSpeed)
                            .orElse(0),
                    motorRequest
                            .getMotor(Location.BACK_RIGHT)
                            .map(Motor::getSpeed)
                            .orElse(0),
                    motorRequest
                            .getMotor(Location.BACK_RIGHT)
                            .map(Motor::getCommand)
                            .orElse(Command.BRAKE)
            );
        }

        System.out.println("Degree\tOpp\tLeft\tRight\tCommand");
        for (int i = 180; i <= 360; i += 15) {
            final MotorRequest motorRequest = motorStrategy.drive(createDriveState(255, i));
            System.out.printf(
                    "%d\t%f\t%d\t%d\t%s\n",
                    i,
                    Math.sin(Math.toRadians(i)) * throttle,
                    motorRequest
                            .getMotor(Location.BACK_LEFT)
                            .map(Motor::getSpeed)
                            .orElse(0),
                    motorRequest
                            .getMotor(Location.BACK_RIGHT)
                            .map(Motor::getSpeed)
                            .orElse(0),
                    motorRequest
                            .getMotor(Location.BACK_RIGHT)
                            .map(Motor::getCommand)
                            .orElse(Command.BRAKE)
            );
        }
    }

    private DriveState createDriveState(final int throttle, final int orientation) {
        return new DriveState(
                new OpenClosedRange(true, true, 0, 255),
                new OpenClosedRange(true, true, 0, 360),
                throttle,
                orientation
        );
    }
}