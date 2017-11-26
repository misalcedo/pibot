package com.salcedo.rapbot.snapshot;

import akka.actor.ActorRef;

import java.time.Instant;
import java.util.*;

public class Snapshot {
    private final Instant start;
    private Instant end;
    private final UUID uuid;
    private final Set<ActorRef> subsystems;
    private final Map<ActorRef, SnapshotMessage> responses;

    public Snapshot(UUID uuid, Set<ActorRef> subsystems) {
        this.uuid = uuid;
        this.subsystems = subsystems;
        this.start = Instant.now();
        this.responses = new LinkedHashMap<>();
    }

    public void addMessage(SnapshotMessage message, ActorRef sender) {
        if (message.getUuid() != uuid || !subsystems.contains(sender)) {
            throw new IllegalArgumentException("Invalid UUID or sender.");
        } else if (responses.containsKey(sender)) {
            throw new IllegalArgumentException("Subsystem already responded to this snapshot.");
        }

        responses.put(sender, message);

        if (isDone()) {
            end = Instant.now();
        }
    }

    public boolean isDone() {
        return responses.size() >= subsystems.size();
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
