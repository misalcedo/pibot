package com.salcedo.rapbot.sense;

import java.util.concurrent.CompletionStage;

public interface SenseService {
    CompletionStage<OrientationResponse> getOrientation();
    CompletionStage<AccelerationResponse> getAcceleration();
}
