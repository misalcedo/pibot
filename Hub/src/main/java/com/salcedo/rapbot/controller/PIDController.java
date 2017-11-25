package com.salcedo.rapbot.controller;

public interface PIDController {
    double step(double actual, double target);
}
