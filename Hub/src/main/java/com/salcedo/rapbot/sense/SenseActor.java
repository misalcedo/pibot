package com.salcedo.rapbot.sense;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.salcedo.rapbot.snapshot.ObjectSnapshotMessage;
import com.salcedo.rapbot.snapshot.RegisterSubSystemMessage;
import com.salcedo.rapbot.snapshot.TakeSnapshotMessage;

import java.util.concurrent.CompletionStage;

public final class SenseActor extends AbstractActor {
    private final SenseService senseService;

    public SenseActor(final SenseService senseService) {
        this.senseService = senseService;
    }

    public static Props props(final SenseService senseService) {
        return Props.create(SenseActor.class, senseService);
    }

    @Override
    public void preStart() {
        context().system().eventStream().publish(new RegisterSubSystemMessage(self()));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrientationRequest.class, this::readOrientation)
                .match(AccelerationRequest.class, r -> readAcceleration())
                .match(TakeSnapshotMessage.class, this::snapshot)
                .build();
    }

    private void readAcceleration() {
        final ActorRef sender = sender();

        senseService.getAcceleration()
                .thenAccept(response -> sender.tell(response, self()));
    }

    private void readOrientation(final OrientationRequest request) {
        final ActorRef sender = sender();
        final CompletionStage<Orientation> orientation = request.isRelative() ?
                senseService.getRelativeOrientation() : senseService.getOrientation();

        orientation.thenAccept(response -> sender.tell(response, self()));
    }

    private void snapshot(final TakeSnapshotMessage message) {
        final ActorRef sender = sender();

        senseService.senseEnvironment()
                .thenAccept(response -> sender.tell(new ObjectSnapshotMessage(message.getUuid(), response), self()));
    }
}
