package com.salcedo.rapbot.snapshot;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.Status;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class SnapshotActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Snapshot snapshot;

    public SnapshotActor(final Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    public static Props props(final Snapshot snapshot) {
        return Props.create(SnapshotActor.class, snapshot);
    }

    private void terminate(Terminated message) {
        final SnapshotMessage snapshotMessage = new ObjectSnapshotMessage(snapshot.getUuid(), message);
        aggregateConditionally(snapshotMessage);
    }

    private void aggregateConditionally(final SnapshotMessage snapshotMessage) {
        if (!snapshot.isSubsystem(sender().path())) {
            log.warning("Received message from an invalid subsystem '{}': {}.", sender().path().toStringWithoutAddress(), snapshotMessage);
            return;
        }

        aggregate(snapshotMessage);
    }

    private void aggregate(final SnapshotMessage message) {
        log.debug("Received message: {}.", message);
        snapshot.addMessage(message, sender().path());

        log.debug("Snapshot '{}' requires {} additional response(s).", snapshot.getUuid(), snapshot.getResponsesRemaining());
        publishSnapshotIfComplete();
    }

    private void publishSnapshotIfComplete() {
        if (!snapshot.isDone()) {
            return;
        }

        log.debug("Completed snapshot '{}'.", snapshot.getUuid());

        getContext().getSystem().eventStream().publish(snapshot);
        getContext().become(createReceive());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SnapshotMessage.class, this::aggregate)
                .match(Status.Failure.class, this::fail)
                .build();
    }

    private void fail(Status.Failure message) {
        final SnapshotMessage snapshotMessage = new ObjectSnapshotMessage(snapshot.getUuid(), message);
        aggregateConditionally(snapshotMessage);
    }
}
