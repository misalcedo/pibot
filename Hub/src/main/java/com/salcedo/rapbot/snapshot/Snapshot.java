package com.salcedo.rapbot.snapshot;

import akka.actor.ActorPath;

import java.time.Instant;
import java.util.*;

import static java.util.function.Predicate.isEqual;

public class Snapshot {
    private final Instant start;
    private final UUID uuid;
    private final Set<ActorPath> subsystems;
    private final Map<ActorPath, SnapshotMessage> responses;
    private Instant end;

    Snapshot(UUID uuid, Set<ActorPath> subsystems) {
        this.uuid = uuid;
        this.subsystems = subsystems;
        this.start = Instant.now();
        this.responses = new LinkedHashMap<>();
    }

    void addMessage(SnapshotMessage message, ActorPath sender) {
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

    int getResponsesRemaining() {
        return subsystems.size() - responses.size();
    }

    public UUID getUuid() {
        return uuid;
    }

    public Instant getStart() {
        return start;
    }

    public Optional<Instant> getEnd() {
        return Optional.ofNullable(end);
    }

    public <T> Optional<T> getSnapshot(final ActorPath subsystem, final Class<? extends T> snapshotType) {
        return Optional.ofNullable(responses.get(subsystem))
                .map(SnapshotMessage::getSnapshot)
                .filter(snapshotType::isInstance)
                .map(snapshotType::cast);
    }

    public Set<ActorPath> getSubsystems() {
        return subsystems;
    }

    public Set<ActorPath> getCompletedSubsystems() {
        return responses.keySet();
    }

    @Override
    public String toString() {
        return "Snapshot{" +
                "first=" + start +
                ", last=" + end +
                ", uuid=" + uuid +
                ", subsystems=" + subsystems +
                ", responses=" + responses +
                '}';
    }

    public boolean isSubsystem(final ActorPath path) {
        return subsystems.stream().anyMatch(isEqual(path));
    }
}
