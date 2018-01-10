package com.salcedo.rapbot.vision;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.salcedo.rapbot.snapshot.ObjectSnapshotMessage;
import com.salcedo.rapbot.snapshot.RegisterSubSystemMessage;
import com.salcedo.rapbot.snapshot.TakeSnapshotMessage;

import java.util.concurrent.CompletionStage;

import static akka.pattern.PatternsCS.pipe;

public final class VisionActor extends AbstractActor {
    private final VisionService visionService;

    public VisionActor(final VisionService visionService) {
        this.visionService = visionService;
    }

    public static Props props(final VisionService visionService) {
        return Props.create(VisionActor.class, visionService);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ImageRequest.class, r -> takePicture())
                .match(TakeSnapshotMessage.class, this::snapshot)
                .build();
    }

    private void takePicture() {
        final ActorRef sender = sender();

        visionService.takePicture()
                .thenAccept(response -> sender.tell(response, self()));
    }

    private void snapshot(final TakeSnapshotMessage message) {
        final CompletionStage<ObjectSnapshotMessage> completionStage = visionService.takePicture()
                .thenApply(response -> new ObjectSnapshotMessage(message.getUuid(), response));
        pipe(completionStage, getContext().dispatcher()).to(sender());
    }
}
