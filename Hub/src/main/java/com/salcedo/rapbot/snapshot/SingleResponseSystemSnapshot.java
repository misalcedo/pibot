package com.salcedo.rapbot.snapshot;

import akka.actor.ActorPath;
import akka.actor.ActorPaths;

import java.time.Instant;
import java.util.*;

import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toSet;

public class SingleResponseSystemSnapshot implements SystemSnapshot {
    private final String system;
    private final Instant start;
    private final UUID uuid;
    private final Set<String> subsystems;
    private final Map<String, Object> responses;
    private Instant end;

    SingleResponseSystemSnapshot(String system, UUID uuid, Set<ActorPath> subsystems) {
        this.system = system;
        this.uuid = uuid;
        this.subsystems = subsystems.stream()
                .map(ActorPath::toStringWithoutAddress)
                .collect(toSet());
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

        responses.put(actor.toStringWithoutAddress(), message.getSnapshot());

        if (isDone()) {
            end = Instant.now();
        }
    }

    private boolean hasResponded(ActorPath actor) {
        return responses.containsKey(actor.toStringWithoutAddress());
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
        return responses.get(subsystem.toStringWithoutAddress());
    }

    @Override
    public Set<ActorPath> getSubsystems() {
        return toActorPaths(subsystems);
    }

    private Set<ActorPath> toActorPaths(Set<String> paths) {
        return paths.stream()
                .map(path -> "akka://" + system + "/" + path)
                .map(ActorPaths::fromString)
                .collect(toSet());
    }

    @Override
    public Set<ActorPath> getCompletedSubsystems() {
        return toActorPaths(responses.keySet());
    }

    @Override
    public boolean isSubsystem(final ActorPath path) {
        return subsystems.stream().anyMatch(isEqual(path.toStringWithoutAddress()));
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
