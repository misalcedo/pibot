package com.salcedo.rapbot.snapshot;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class SnapshotActor extends AbstractActor {
    private static final FiniteDuration RECEIVE_TIMEOUT = Duration.create(150L, MILLISECONDS);
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Set<ActorRef> subSystems;
    private final Map<UUID, Snapshot> snapshots;

    public SnapshotActor() {
        this.subSystems = new LinkedHashSet<>();
        this.snapshots = new HashMap<>();
    }

    public static Props props() {
        return Props.create(SnapshotActor.class);
    }

    @Override
    public void preStart() {
        subSystems.clear();
        snapshots.clear();

        setTimeout();
    }

    private void setTimeout() {
        context().setReceiveTimeout(RECEIVE_TIMEOUT);
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
            log.info("Timed out waiting for snapshot to complete. IDs: {}", snapshots.keySet());
            snapshots.clear();
            setTimeout();
        } else {
            startSnapshot();
        }
    }

    private void startSnapshot() {
        if (snapshotInProgress()) {
            final Snapshot snapshot = snapshots.values().iterator().next();

            log.debug(
                    "Snapshot already in progress. ID: {}, Remaining subsystems: {}",
                    snapshot.getUuid(),
                    snapshot.getResponsesRemaining()
            );

            return;
        }

        final UUID uuid = UUID.randomUUID();
        final Set<ActorPath> paths = subSystems.stream().map(ActorRef::path).collect(toSet());

        if (snapshots.containsKey(uuid)) {
            log.warning("Snapshot already started. Subsystems: {}, UUID: {}", subSystems, uuid);
            return;
        }

        snapshots.put(uuid, new Snapshot(uuid, paths));

        log.debug("Starting snapshot '{}'. Subsystems: {}", uuid, paths);

        context().setReceiveTimeout(Duration.create(1L, SECONDS));

        subSystems.forEach(subSystem -> subSystem.tell(new TakeSnapshotMessage(uuid), self()));
    }

    private boolean snapshotInProgress() {
        return !snapshots.isEmpty();
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
        final Snapshot snapshot = snapshots.get(message.getId());

        log.debug("Received new snapshot message {}", message);

        if (snapshot == null || snapshot.isDone()) {
            log.error("Received snapshot message for an invalid snapshot. Message: {}, Snapshot: {}", message, snapshot);
        } else {
            snapshot.addMessage(message, sender().path());

            if (snapshot.isDone()) {
                log.debug("Completed snapshot '{}'.", snapshot.getUuid());
                getContext().getSystem().eventStream().publish(snapshot);
                snapshots.remove(snapshot.getUuid());
                setTimeout();
            } else {
                log.debug("Snapshot '{}' requires {} additional response(s).", snapshot.getUuid(), snapshot.getResponsesRemaining());
            }
        }
    }
}
