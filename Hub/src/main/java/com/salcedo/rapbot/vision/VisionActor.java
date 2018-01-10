package com.salcedo.rapbot.vision;

import akka.actor.Props;
import com.salcedo.rapbot.hub.ServiceClientActor;

import java.util.concurrent.CompletionStage;

public final class VisionActor extends ServiceClientActor {
    private final VisionService visionService;

    public VisionActor(final VisionService visionService) {
        this.visionService = visionService;
    }

    public static Props props(final VisionService visionService) {
        return Props.create(VisionActor.class, visionService);
    }

    @Override
    public Receive createReceive() {
        return baseReceiveBuilder()
                .match(ImageRequest.class, r -> takePicture())
                .build();
    }

    private void takePicture() {
        pipeToSender(visionService::takePicture);
    }

    @Override
    protected CompletionStage<?> snapshot() {
        return visionService.takePicture();
    }
}
