package com.salcedo.rapbot.driver;

public interface DriverStrategy<T> {
    DriveState drive(T request);
}
