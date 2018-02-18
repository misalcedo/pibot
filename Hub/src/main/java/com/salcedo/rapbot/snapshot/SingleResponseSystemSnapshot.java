package com.salcedo.rapbot.snapshot;

import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.Status;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toSet;

public class SingleResponseSystemSnapshot implements SystemSnapshot {
    private final Instant start;
    private final UUID uuid;
    private final Set<ActorPath> subsystems;
    private final Map<ActorPath, Object> responses;
    private Instant end;

    public SingleResponseSystemSnapshot(UUID uuid, Set<ActorPath> subsystems) {
        this.uuid = uuid;
        this.subsystems = subsystems;
        this.start = Instant.now();
        this.responses = new LinkedHashMap<>();
    }

    @Override
    public void addSnapshot(SnapshotMessage message, ActorPath actor) {
        if (message.getId() != uuid || !isSubsystem(actor)) {
            throw new IllegalArgumentException("Invalid UUID or actor. Actor: " + actor + ", UUID: " + uuid + ".");
        } else if (hasResponded(actor)) {
            throw new IllegalArgumentException("Subsystem already responded to this snapshot.");
        }

        responses.put(actor, message.getSnapshot());

        if (isDone()) {
            end = Instant.now();
        }
    }

    @Override
    public void addSnapshot(SnapshotMessage message, ActorRef sender) {
        addSnapshot(message, sender.path());
    }

    private boolean hasResponded(ActorPath actor) {
        return responses.containsKey(actor);
    }

    @Override
    public boolean isDone() {
        return getResponsesRemaining() <= 0;
    }

    @Override
    public int getResponsesRemaining() {
        return subsystems.size() - responses.size();
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public Instant getStart() {
        return start;
    }

    @Override
    public Optional<Instant> getEnd() {
        return Optional.ofNullable(end);
    }

    @Override
    public <T> Optional<T> getSnapshot(final ActorPath subsystem, final Class<? extends T> snapshotType) {
        return Optional.ofNullable(getSnapshot(subsystem))
                .filter(snapshotType::isInstance)
                .map(snapshotType::cast);
    }

    private Object getSnapshot(ActorPath subsystem) {
        return responses.get(subsystem);
    }

    @Override
    public Set<ActorPath> getSubsystems() {
        return subsystems;
    }

    @Override
    public Set<ActorPath> getCompletedSubsystems() {
        return responses.entrySet().stream()
                .filter(entry -> !Status.Failure.class.isInstance(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(toSet());
    }

    @Override
    public boolean isSubsystem(final ActorPath path) {
        return subsystems.stream().anyMatch(isEqual(path));
    }

    @Override
    public boolean isSubsystem(ActorRef sender) {
        return isSubsystem(sender.path());
    }

    @Override
    public String toString() {
        return "SystemSnapshot{" +
                "first=" + start +
                ", last=" + end +
                ", uuid=" + uuid +
                ", subsystems=" + subsystems +
                ", responses=" + responses +
                '}';
    }
}
