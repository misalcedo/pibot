package com.salcedo.rapbot.snapshot;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toSet;

public class SnapshotActor extends AbstractActor {
    private static final FiniteDuration RECEIVE_TIMEOUT = Duration.create(250L, MILLISECONDS);
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Set<ActorRef> subSystems;
    private Snapshot snapshot;

    public SnapshotActor() {
        this.subSystems = new LinkedHashSet<>();
        this.snapshot = null;
    }

    public static Props props() {
        return Props.create(SnapshotActor.class);
    }

    @Override
    public void preStart() {
        subSystems.clear();
        clearSnapshot();
        setTimeout();
    }

    private void setTimeout() {
        context().setReceiveTimeout(RECEIVE_TIMEOUT);
    }

    private void clearSnapshot() {
        this.snapshot = null;
    }

    private void unregisterAndFail(Terminated message) {
        unregister(message);

        final String actor = message.getActor().path().toStringWithoutAddress();
        final Status.Failure failure = new Status.Failure(new RuntimeException(actor));

        fail(failure);
    }

    /**
     * Unregisters a subsystem. Does not affect ongoing snapshots.
     *
     * @param message The subsystem to unregister.
     */
    private void unregister(Terminated message) {
        subSystems.remove(message.actor());

        log.warning("Removed {} from subsystems due to termination.", message.actor());
    }

    private void fail(Status.Failure message) {
        log.error(
                "Received a failure for some subsystem. Failure cause: {}. Snapshot: {}.",
                message.cause(),
                snapshot
        );

        snapshot.addFailure(message);
        publishSnapshotIfComplete();
    }

    private void publishSnapshotIfComplete() {
        if (!snapshot.isDone()) {
            return;
        }

        log.debug("Completed snapshot '{}'.", snapshot.getUuid());

        getContext().getSystem().eventStream().publish(snapshot);
        getContext().become(createReceive());
        clearSnapshot();
        setTimeout();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RegisterSubSystemMessage.class, this::register)
                .match(Terminated.class, this::unregister)
                .match(StartSnapshotMessage.class, m -> startSnapshot())
                .match(ReceiveTimeout.class, m -> startSnapshot())
                .build();
    }

    /**
     * Creates a snapshot.
     */
    private void startSnapshot() {
        removeTimeout();

        final UUID uuid = UUID.randomUUID();
        final Set<ActorPath> paths = subSystems.stream().map(ActorRef::path).collect(toSet());

        snapshot = new Snapshot(uuid, paths);

        log.debug("Starting snapshot '{}'. Subsystems: {}.", uuid, paths);

        subSystems.forEach(subSystem -> subSystem.tell(new TakeSnapshotMessage(uuid), self()));
        getContext().become(createSnapshotReceive());
    }

    private Receive createSnapshotReceive() {
        return receiveBuilder()
                .match(RegisterSubSystemMessage.class, this::register)
                .match(Terminated.class, this::unregisterAndFail)
                .match(ObjectSnapshotMessage.class, this::aggregate)
                .match(Status.Failure.class, this::fail)
                .build();
    }

    private void removeTimeout() {
        context().setReceiveTimeout(Duration.Undefined());
    }

    /**
     * Registers a subsystem for future snapshots. Any ongoing snapshots are unaffected.
     *
     * @param message The subsystem to register.
     */
    private void register(final RegisterSubSystemMessage message) {
        subSystems.add(message.getSubSystem());
        context().watch(message.getSubSystem());
    }

    /**
     * Aggregates snapshot messages for the ongoing snapshot.
     *
     * @param message The snapshot for a subsystem.
     */
    private void aggregate(final ObjectSnapshotMessage message) {
        log.debug("Received message: {}.", message);
        snapshot.addMessage(message, sender().path());
        log.debug("Snapshot '{}' requires {} additional response(s).", snapshot.getUuid(), snapshot.getResponsesRemaining());
        publishSnapshotIfComplete();
    }
}
