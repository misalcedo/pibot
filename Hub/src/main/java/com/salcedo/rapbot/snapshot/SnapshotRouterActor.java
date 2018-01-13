package com.salcedo.rapbot.snapshot;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toSet;

public class SnapshotRouterActor extends AbstractActor {
    private static final FiniteDuration RECEIVE_TIMEOUT = Duration.create(250L, MILLISECONDS);
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Set<ActorRef> subSystems;
    private final Map<UUID, Routee> snapshots;
    private Router router;

    public SnapshotRouterActor() {
        this.subSystems = new LinkedHashSet<>();
        this.snapshots = new HashMap<>();
        this.router = new Router(new BroadcastRoutingLogic());
    }

    public static Props props() {
        return Props.create(SnapshotRouterActor.class);
    }

    @Override
    public void preStart() {
        getContext().getSystem().eventStream().subscribe(self(), SystemSnapshot.class);
        setTimeout();
    }

    private void setTimeout() {
        if (snapshots.isEmpty()) {
            context().setReceiveTimeout(RECEIVE_TIMEOUT);
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RegisterSubSystemMessage.class, this::register)
                .match(Terminated.class, this::unregister)
                .match(StartSnapshotMessage.class, m -> startSnapshot())
                .match(ReceiveTimeout.class, m -> startSnapshot())
                .match(SystemSnapshot.class, this::finishSnapshot)
                .build();
    }

    private void startSnapshot() {
        removeTimeout();

        final UUID uuid = UUID.randomUUID();
        final Set<ActorPath> paths = subSystems.stream().map(ActorRef::path).collect(toSet());

        log.debug("Starting systemSnapshot '{}'. Subsystems: {}.", uuid, paths);

        final SystemSnapshot systemSnapshot = new SingleResponseSystemSnapshot(uuid, paths);
        final ActorRef routee = getContext().actorOf(SnapshotActor.props(systemSnapshot));

        addRoutee(uuid, routee);
        subSystems.forEach(subSystem -> subSystem.tell(new TakeSnapshotMessage(uuid), routee));
    }

    private void addRoutee(UUID uuid, ActorRef actor) {
        final Routee routee = new ActorRefRoutee(actor);

        router = router.addRoutee(routee);

        snapshots.put(uuid, routee);
    }

    private void removeTimeout() {
        context().setReceiveTimeout(Duration.Undefined());
    }

    private void unregister(Terminated message) {
        subSystems.remove(message.actor());

        snapshots.values().forEach(routee -> routee.send(message, message.actor()));
        router.route(new Status.Failure(new IllegalStateException("Terminated")), message.actor());

        log.warning("Removed {} from subsystems due to termination.", message.actor());
    }

    private void register(final RegisterSubSystemMessage message) {
        subSystems.add(message.getSubSystem());
        context().watch(message.getSubSystem());
    }

    private void finishSnapshot(final SystemSnapshot systemSnapshot) {
        final Routee routee = snapshots.remove(systemSnapshot.getUuid());

        removeRoutee(routee);
        routee.send(PoisonPill.getInstance(), self());
        setTimeout();

        log.debug("Completed systemSnapshot: {}.", systemSnapshot);
    }

    private void removeRoutee(Routee routee) {
        router = router.removeRoutee(routee);
    }

}