package com.salcedo.rapbot.snapshot;

import akka.actor.ActorRef;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Snapshot {
    private final Instant start;
    private final UUID uuid;
    private final Set<ActorRef> subsystems;
    private final List<SnapshotMessage> responses;

    public Snapshot(Instant start, UUID uuid, Set<ActorRef> subsystems) {
        this.start = start;
        this.uuid = uuid;
        this.subsystems = subsystems;
        this.responses = new LinkedList<>();
    }

    public void addMessage(SnapshotMessage message, ActorRef sender) {
        if (message.getUuid() != uuid || !subsystems.contains(sender)) {
            throw new IllegalArgumentException("Invalid UUID.");
        }

        responses.add(message);
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
