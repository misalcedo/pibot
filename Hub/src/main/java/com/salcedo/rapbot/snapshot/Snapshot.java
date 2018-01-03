package com.salcedo.rapbot.snapshot;

import akka.actor.ActorPath;

import java.time.Instant;
import java.util.*;

public class Snapshot {
    private final Instant start;
    private Instant end;
    private final UUID uuid;
    private final Set<ActorPath> subsystems;
    private final Map<ActorPath, SnapshotMessage> responses;

    Snapshot(UUID uuid, Set<ActorPath> subsystems) {
        this.uuid = uuid;
        this.subsystems = subsystems;
        this.start = Instant.now();
        this.responses = new LinkedHashMap<>();
    }

    void addMessage(ObjectSnapshotMessage message, ActorPath sender) {
        if (message.getId() != uuid || !subsystems.contains(sender)) {
            throw new IllegalArgumentException("Invalid UUID or sender.");
        } else if (responses.containsKey(sender)) {
            throw new IllegalArgumentException("Subsystem already responded to this snapshot.");
        }

        responses.put(sender, message);

        if (isDone()) {
            end = Instant.now();
        }
    }

    boolean isDone() {
        return getResponsesRemaining() <= 0;
    }

    UUID getUuid() {
        return uuid;
    }

    int getResponsesRemaining() {
        return subsystems.size() - responses.size();
    }

    public <T> T getSnapshot(final ActorPath subsystem, final Class<? extends T> snapshotType) {
        SnapshotMessage snapshotMessage = responses.getOrDefault(subsystem, new NullObjectSnapshotMessage(uuid));

        return snapshotMessage.getSnapshot(snapshotType);
    }

    @Override
    public String toString() {
        return "Snapshot{" +
                "start=" + start +
                ", end=" + end +
                ", uuid=" + uuid +
                ", subsystems=" + subsystems +
                ", responses=" + responses +
                '}';
    }
}
