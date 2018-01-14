package com.salcedo.rapbot.snapshot;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toSet;

public class SnapshotRouterActor extends AbstractActor {
    private static final FiniteDuration RECEIVE_TIMEOUT = Duration.create(250L, MILLISECONDS);
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Set<ActorRef> subSystems;
    private Router router;

    public SnapshotRouterActor() {
        this.subSystems = new LinkedHashSet<>();
        this.router = new Router(new BroadcastRoutingLogic());
    }

    public static Props props() {
        return Props.create(SnapshotRouterActor.class);
    }

    @Override
    public void preStart() {
        setTimeout();
    }

    private void setTimeout() {
        if (router.routees().isEmpty()) {
            context().setReceiveTimeout(RECEIVE_TIMEOUT);
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RegisterSubSystemMessage.class, this::register)
                .match(Terminated.class, this::terminate)
                .match(StartSnapshotMessage.class, m -> startSnapshot())
                .match(ReceiveTimeout.class, m -> startSnapshot())
                .build();
    }

    private void startSnapshot() {
        removeTimeout();

        final UUID uuid = UUID.randomUUID();
        final Set<ActorPath> paths = subSystems.stream().map(ActorRef::path).collect(toSet());

        log.debug("Starting systemSnapshot '{}'. Subsystems: {}.", uuid, paths);

        final SystemSnapshot systemSnapshot = new SingleResponseSystemSnapshot(uuid, paths);
        final ActorRef routee = getContext().actorOf(SnapshotActor.props(systemSnapshot));

        addRoutee(routee);
        subSystems.forEach(subSystem -> subSystem.tell(new TakeSnapshotMessage(uuid), routee));
    }

    private void addRoutee(ActorRef actor) {
        getContext().watch(actor);
        router = router.addRoutee(new ActorRefRoutee(actor));
    }

    private void removeTimeout() {
        context().setReceiveTimeout(Duration.Undefined());
    }

    private void terminate(Terminated message) {
        if (subSystems.contains(message.actor())) {
            unregister(message);
        } else {
            finishSnapshot(message);
        }
    }

    private void unregister(Terminated message) {
        subSystems.remove(message.actor());

        router.route(new Status.Failure(new IllegalStateException("Terminated")), message.actor());

        log.warning("Removed {} from subsystems due to termination.", message.actor());
    }

    private void finishSnapshot(final Terminated message) {
        router = router.removeRoutee(message.actor());
        setTimeout();
    }

    private void register(final RegisterSubSystemMessage message) {
        subSystems.add(message.getSubSystem());
        context().watch(message.getSubSystem());
    }
}