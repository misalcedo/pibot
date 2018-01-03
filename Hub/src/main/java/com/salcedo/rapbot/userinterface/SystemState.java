package com.salcedo.rapbot.userinterface;

import com.salcedo.rapbot.driver.DriveState;

public interface SystemState {
    double getTemperature();

    DriveState getDriveState();
}
