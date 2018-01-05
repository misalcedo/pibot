package com.salcedo.rapbot.sense;

import java.util.concurrent.CompletionStage;

public interface SenseService {
    CompletionStage<EnvironmentReading> senseEnvironment();
    CompletionStage<Orientation> getOrientation();
    CompletionStage<Orientation> getRelativeOrientation();
    CompletionStage<ThreeDimensionalSensorReading> getAcceleration();
}
