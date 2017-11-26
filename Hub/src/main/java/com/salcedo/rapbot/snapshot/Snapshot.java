package com.salcedo.rapbot.snapshot;

import akka.actor.ActorRef;

import java.time.Instant;
import java.util.*;

public class Snapshot {
    private final Instant start;
    private final UUID uuid;
    private final Set<ActorRef> subsystems;
    private final Map<ActorRef, SnapshotMessage> responses;

    public Snapshot(Instant start, UUID uuid, Set<ActorRef> subsystems) {
        this.start = start;
        this.uuid = uuid;
        this.subsystems = subsystems;
        this.responses = new LinkedHashMap<>();
    }

    public void addMessage(SnapshotMessage message, ActorRef sender) {
        if (message.getUuid() != uuid || !subsystems.contains(sender)) {
            throw new IllegalArgumentException("Invalid UUID or sender.");
        } else if (responses.containsKey(sender)) {
            throw new IllegalArgumentException("Subsystem already responded to this snapshot.");
        }

        responses.put(sender, message);
    }

    public boolean isDone() {
        return responses.size() >= subsystems.size();
    }

    @Override
    public String toString() {
        return "Snapshot{" +
                "start=" + start +
                ", uuid=" + uuid +
                ", subsystems=" + subsystems +
                ", responses=" + responses +
                '}';
    }
}
