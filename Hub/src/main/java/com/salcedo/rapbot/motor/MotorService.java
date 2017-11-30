package com.salcedo.rapbot.motor;

import java.util.concurrent.CompletionStage;

public interface MotorService {
    CompletionStage<MotorResponse> drive(MotorRequest request);
    CompletionStage<MotorResponse> release();
    CompletionStage<MotorResponse> state();
}
