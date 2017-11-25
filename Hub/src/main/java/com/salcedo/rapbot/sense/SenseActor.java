package com.salcedo.rapbot.sense;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;

public final class SenseActor extends AbstractActor {
    private final SenseService senseService;

    public SenseActor(final SenseService senseService) {
        this.senseService = senseService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrientationRequest.class, r -> readOrientation())
                .build();
    }

    private void readOrientation() {
        final ActorRef sender = sender();

        senseService.getOrientation()
                .thenAccept(response -> sender.tell(response, self()));
    }
}
