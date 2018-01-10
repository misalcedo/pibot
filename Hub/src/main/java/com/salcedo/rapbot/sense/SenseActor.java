package com.salcedo.rapbot.sense;

import akka.actor.Props;
import com.salcedo.rapbot.hub.ServiceClientActor;

import java.util.concurrent.CompletionStage;

public final class SenseActor extends ServiceClientActor {
    private final SenseService senseService;

    public SenseActor(final SenseService senseService) {
        this.senseService = senseService;
    }

    public static Props props(final SenseService senseService) {
        return Props.create(SenseActor.class, senseService);
    }

    @Override
    public Receive createReceive() {
        return baseReceiveBuilder()
                .match(OrientationRequest.class, r -> readOrientation())
                .match(AccelerationRequest.class, r -> readAcceleration())
                .build();
    }

    private void readAcceleration() {
        pipeToSender(senseService::getAcceleration);
    }

    private void readOrientation() {
        pipeToSender(senseService::getOrientation);
    }

    @Override
    protected CompletionStage<?> snapshot() {
        return senseService.senseEnvironment();
    }
}
