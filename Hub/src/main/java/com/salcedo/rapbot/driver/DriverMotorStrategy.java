package com.salcedo.rapbot.driver;

import com.salcedo.rapbot.locomotion.MotorRequest;
import com.salcedo.rapbot.sense.Orientation;

public interface DriverMotorStrategy {
    MotorRequest drive(DriveState driveState);
}
