package com.salcedo.rapbot.locomotion;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.snapshot.ObjectSnapshotMessage;
import com.salcedo.rapbot.snapshot.TakeSnapshotMessage;

import java.util.concurrent.CompletionStage;

import static akka.pattern.PatternsCS.pipe;

public final class MotorActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
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
        return receiveBuilder()
                .match(MotorRequest.class, this::drive)
                .match(TakeSnapshotMessage.class, this::snapshot)
                .build();
    }

    private void snapshot(TakeSnapshotMessage message) {
        final CompletionStage<ObjectSnapshotMessage> completionStage = motorService.state()
                .thenApply(response -> new ObjectSnapshotMessage(message.getUuid(), response));
        pipe(completionStage, getContext().dispatcher()).to(sender());
    }

    private void drive(final MotorRequest request) {
        motorService.drive(request);
    }
}
