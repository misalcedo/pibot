package com.salcedo.rapbot.snapshot;

import akka.actor.ActorPath;
import akka.actor.ActorRef;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SystemSnapshot {
    void addSnapshot(SnapshotMessage message, ActorPath sender);

    void addSnapshot(SnapshotMessage message, ActorRef sender);

    boolean isDone();

    int getResponsesRemaining();

    UUID getUuid();

    Instant getStart();

    Optional<Instant> getEnd();

    <T> Optional<T> getSnapshot(final ActorPath subsystem, final Class<? extends T> snapshotType);

    Set<ActorPath> getSubsystems();

    Set<ActorPath> getCompletedSubsystems();

    boolean isSubsystem(final ActorPath path);
    boolean isSubsystem(final ActorRef sender);
}
