package com.salcedo.rapbot.snapshot;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.time.Instant;
import java.util.*;

import static java.util.Collections.unmodifiableSet;

public class SnapshotActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Set<ActorRef> subSystems;
    private final Map<UUID, Snapshot> snapshots;

    public SnapshotActor() {
        this.subSystems = new LinkedHashSet<>();
        this.snapshots = new HashMap<>();
    }

    @Override
    public void preStart() throws Exception {
        subSystems.clear();
        snapshots.clear();

        context().system().eventStream().subscribe(self(), RegisterSubSystemMessage.class);
        context().system().eventStream().subscribe(self(), StartSnapshotMessage.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartSnapshotMessage.class, this::startSnapshot)
                .match(RegisterSubSystemMessage.class, message -> subSystems.add(message.getSubSystem()))
                .match(SnapshotMessage.class, this::aggregate)
                .build();
    }

    private void aggregate(final SnapshotMessage message) {
        final Snapshot snapshot = snapshots.get(message.getUuid());

        log.info("Received new snapshot message. Snapshot: {}, Message: {}", snapshot, message);

        if (snapshot == null || snapshot.isDone()) {
            log.error("Received snapshot message for an invalid snapshot. Message: {}, Snapshot: {}", message, snapshot);
        } else {
            snapshot.addMessage(message, sender());

            if (snapshot.isDone()) {
                log.info("Snapshot '{}' completed.", snapshot);
            }
        }
    }

    private void startSnapshot(StartSnapshotMessage message) {
        final UUID uuid = UUID.randomUUID();

        if (snapshots.containsKey(uuid)) {
            log.info("Snapshot already started. Subsystems: {}, UUID: {}", subSystems, uuid);
        }

        snapshots.put(uuid, new Snapshot(uuid, unmodifiableSet(subSystems)));

        log.info("Starting snapshot '{}'. Subsystems: {}", uuid, subSystems);

        subSystems.forEach(subSystem -> subSystem.tell(new TakeSnapshotMessage(uuid), self()));
    }
}
