package com.salcedo.rapbot.sense;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.salcedo.rapbot.snapshot.RegisterSubSystemMessage;
import com.salcedo.rapbot.snapshot.SnapshotMessage;
import com.salcedo.rapbot.snapshot.TakeSnapshotMessage;

public final class SenseActor extends AbstractActor {
    private final SenseService senseService;

    public SenseActor(final SenseService senseService) {
        this.senseService = senseService;
    }

    @Override
    public void preStart() throws Exception {
        context().system().eventStream().publish(new RegisterSubSystemMessage(self()));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrientationRequest.class, r -> readOrientation())
                .match(AccelerationRequest.class, r -> readAcceleration())
                .match(TakeSnapshotMessage.class, this::snapshot)
                .build();
    }

    private void snapshot(final TakeSnapshotMessage message) {
        final ActorRef sender = sender();

        senseService.getOrientation()
                .thenAccept(response -> sender.tell(new SnapshotMessage(message.getUuid(), response), self()));
    }

    private void readAcceleration() {
        final ActorRef sender = sender();

        senseService.getAcceleration()
                .thenAccept(response -> sender.tell(response, self()));
    }

    private void readOrientation() {
        final ActorRef sender = sender();

        senseService.getOrientation()
                .thenAccept(response -> sender.tell(response, self()));
    }
}
