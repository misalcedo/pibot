package com.salcedo.rapbot.sense;

import akka.actor.AbstractActor;

public final class SenseActor extends AbstractActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrientationRequest.class, this::readOrientation)
                .build();
    }

    private void readOrientation(final OrientationRequest request) {

    }
}
