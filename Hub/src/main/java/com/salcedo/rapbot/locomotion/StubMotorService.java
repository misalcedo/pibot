package com.salcedo.rapbot.locomotion;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.CompletableFuture.completedFuture;

public final class StubMotorService implements MotorService {
    private static final MotorResponse INITIAL_RESPONSE = MotorResponse.builder()
            .addMotor(Motor.builder().withBackLeftLocation().withReleaseCommand().withSpeed(0).build())
            .addMotor(Motor.builder().withBackRightLocation().withReleaseCommand().withSpeed(0).build())
            .build();
    private final AtomicReference<MotorResponse> response;

    StubMotorService() {
        this.response = new AtomicReference<>(INITIAL_RESPONSE);
    }

    @Override
    public CompletionStage<MotorResponse> drive(MotorRequest request) {
        response.set(new MotorResponse(request.getMotors()));
        return completedFuture(response.get());
    }

    @Override
    public CompletionStage<MotorResponse> release() {
        response.set(INITIAL_RESPONSE);
        return completedFuture(response.get());
    }

    @Override
    public CompletionStage<MotorResponse> state() {
        return completedFuture(response.get());
    }
}
