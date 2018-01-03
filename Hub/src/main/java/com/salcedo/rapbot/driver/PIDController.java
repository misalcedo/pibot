package com.salcedo.rapbot.driver;

public interface PIDController {
    default PIDResult step(double actual, double target) {
        return step(actual, target, new PIDResult(actual));
    }

    PIDResult step(double actual, double target, PIDResult previousStep);
}
