package com.salcedo.rapbot.locomotion;

import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.hub.ServiceClientActor;

import java.util.concurrent.CompletionStage;

public final class MotorActor extends ServiceClientActor {
    private final MotorService motorService;

    public MotorActor(final MotorService motorService) {
        this.motorService = motorService;
    }

    public static Props props(final MotorService motorService) {
        return Props.create(MotorActor.class, motorService);
    }

    @Override
    public void preStart() {
        release();
    }

    private void release() {
        motorService.release();
    }

    @Override
    public void postStop() {
        release();
    }

    @Override
    public Receive createReceive() {
        return baseReceiveBuilder()
                .match(MotorRequest.class, this::drive)
                .build();
    }

    @Override
    protected CompletionStage<?> snapshot() {
        return motorService.state();
    }

    private void drive(final MotorRequest request) {
        callWithBreaker(() -> motorService.drive(request));
    }
}
