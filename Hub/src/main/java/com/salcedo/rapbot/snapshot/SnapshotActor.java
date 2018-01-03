package com.salcedo.rapbot.snapshot;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.*;

import static java.util.stream.Collectors.toSet;

public class SnapshotActor extends AbstractActor {
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

        context().system().eventStream().subscribe(self(), RegisterSubSystemMessage.class);
        context().system().eventStream().subscribe(self(), StartSnapshotMessage.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartSnapshotMessage.class, m -> startSnapshot())
                .match(RegisterSubSystemMessage.class, this::register)
                .match(ObjectSnapshotMessage.class, this::aggregate)
                .match(Terminated.class, this::unregister)
                .build();
    }

    private void unregister(Terminated message) {
        subSystems.remove(message.actor());

        log.info("Removed {} from subsystems due to termination.", message.actor());
    }

    private void register(RegisterSubSystemMessage message) {
        subSystems.add(message.getSubSystem());
        context().watch(message.getSubSystem());
    }

    private void aggregate(final ObjectSnapshotMessage message) {
        final Snapshot snapshot = snapshots.get(message.getId());

        log.info("Received new snapshot message {}", message);

        if (snapshot == null || snapshot.isDone()) {
            log.error("Received snapshot message for an invalid snapshot. Message: {}, Snapshot: {}", message, snapshot);
        } else {
            snapshot.addMessage(message, sender().path());

            if (snapshot.isDone()) {
                log.info("Completed snapshot '{}'.", snapshot.getUuid());
                getContext().getSystem().eventStream().publish(snapshot);
            } else {
                log.info("Snapshot '{}' requires {} additional response(s).", snapshot.getUuid(), snapshot.getResponsesRemaining());
            }
        }
    }

    private void startSnapshot() {
        final UUID uuid = UUID.randomUUID();
        final Set<ActorPath> paths = subSystems.stream().map(ActorRef::path).collect(toSet());

        if (snapshots.containsKey(uuid)) {
            log.info("Snapshot already started. Subsystems: {}, UUID: {}", subSystems, uuid);
        }

        snapshots.put(uuid, new Snapshot(uuid, paths));

        log.info("Starting snapshot '{}'. Subsystems: {}", uuid, paths);

        subSystems.forEach(subSystem -> subSystem.tell(new TakeSnapshotMessage(uuid), self()));
    }
}
