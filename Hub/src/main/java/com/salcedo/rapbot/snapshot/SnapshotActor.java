package com.salcedo.rapbot.snapshot;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toSet;

public class SnapshotActor extends AbstractActor {
    private static final FiniteDuration RECEIVE_TIMEOUT = Duration.create(250L, MILLISECONDS);
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Set<ActorRef> subSystems;
    private Snapshot snapshot;

    public SnapshotActor() {
        this.subSystems = new LinkedHashSet<>();
        this.snapshot = null;
    }

    public static Props props() {
        return Props.create(SnapshotActor.class);
    }

    @Override
    public void preStart() {
        subSystems.clear();
        clearSnapshot();
        setTimeout();
    }

    private void setTimeout() {
        context().setReceiveTimeout(RECEIVE_TIMEOUT);
    }

    private void clearSnapshot() {
        this.snapshot = null;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartSnapshotMessage.class, m -> startSnapshot())
                .match(RegisterSubSystemMessage.class, this::register)
                .match(ObjectSnapshotMessage.class, this::aggregate)
                .match(Terminated.class, this::unregister)
                .match(ReceiveTimeout.class, m -> receiveTimeout())
                .build();
    }

    private void receiveTimeout() {
        if (snapshotInProgress()) {
            log.info(
                    "Timed out waiting for snapshot to complete. ID: {}, Responses Remaining: {}",
                    snapshot.getUuid(),
                    snapshot.getResponsesRemaining()
            );

            publishSnapshot();
        } else {
            startSnapshot();
        }
    }

    private void startSnapshot() {
        if (snapshotInProgress()) {
            log.debug(
                    "Snapshot already in progress. ID: {}, Remaining subsystems: {}",
                    snapshot.getUuid(),
                    snapshot.getResponsesRemaining()
            );

            return;
        }

        final UUID uuid = UUID.randomUUID();
        final Set<ActorPath> paths = subSystems.stream().map(ActorRef::path).collect(toSet());

        snapshot = new Snapshot(uuid, paths);

        log.debug("Starting snapshot '{}'. Subsystems: {}", uuid, paths);

        context().setReceiveTimeout(Duration.create(1L, SECONDS));

        subSystems.forEach(subSystem -> subSystem.tell(new TakeSnapshotMessage(uuid), self()));
    }

    private boolean snapshotInProgress() {
        return snapshot != null;
    }

    private void unregister(Terminated message) {
        subSystems.remove(message.actor());

        log.warning("Removed {} from subsystems due to termination.", message.actor());
    }

    private void register(RegisterSubSystemMessage message) {
        subSystems.add(message.getSubSystem());
        context().watch(message.getSubSystem());
    }

    private void aggregate(final ObjectSnapshotMessage message) {
        if (!snapshotInProgress() || snapshot.isDone()) {
            log.error("Received snapshot message for an invalid snapshot. Message: {}, Snapshot: {}", message, snapshot);
            return;
        }

        snapshot.addMessage(message, sender().path());
        log.debug("Snapshot '{}' requires {} additional response(s).", snapshot.getUuid(), snapshot.getResponsesRemaining());

        if (snapshot.isDone()) {
            log.debug("Completed snapshot '{}'.", snapshot.getUuid());
            publishSnapshot();
        }
    }

    private void publishSnapshot() {
        getContext().getSystem().eventStream().publish(snapshot);
        clearSnapshot();
        setTimeout();
    }
}
