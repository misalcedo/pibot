package com.salcedo.rapbot.snapshot;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.Status;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class SnapshotActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final SystemSnapshot systemSnapshot;

    public SnapshotActor(final SystemSnapshot systemSnapshot) {
        this.systemSnapshot = systemSnapshot;
    }

    public static Props props(final SystemSnapshot systemSnapshot) {
        return Props.create(SnapshotActor.class, systemSnapshot);
    }

    private void aggregateConditionally(final SnapshotMessage snapshotMessage) {
        if (!systemSnapshot.isSubsystem(sender().path())) {
            log.warning("Received message from an invalid subsystem '{}': {}.", sender().path().toStringWithoutAddress(), snapshotMessage);
            return;
        }

        aggregate(snapshotMessage);
    }

    private void aggregate(final SnapshotMessage message) {
        log.debug("Received message: {}.", message);
        systemSnapshot.addSnapshot(message, sender().path());

        log.debug("SystemSnapshot '{}' requires {} additional response(s).", systemSnapshot.getUuid(), systemSnapshot.getResponsesRemaining());
        publishSnapshotIfComplete();
    }

    private void publishSnapshotIfComplete() {
        if (!systemSnapshot.isDone()) {
            return;
        }

        log.debug("Completed systemSnapshot '{}'.", systemSnapshot.getUuid());

        getContext().getSystem().eventStream().publish(systemSnapshot);
        getContext().stop(self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SnapshotMessage.class, this::aggregate)
                .match(Status.Failure.class, this::fail)
                .build();
    }

    private void fail(final Status.Failure message) {
        log.debug("Received failure for {}. Failure: {}", sender(), message.cause());
        aggregateConditionally(new ObjectSnapshotMessage(systemSnapshot.getUuid(), message));
    }
}
