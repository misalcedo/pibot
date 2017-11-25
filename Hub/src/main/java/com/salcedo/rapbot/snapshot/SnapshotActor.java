package com.salcedo.rapbot.snapshot;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SnapshotActor extends AbstractActor {
    private static final FiniteDuration DELAY = Duration.create(500L, TimeUnit.MILLISECONDS);
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final List<ActorRef> subSystems;
    private int responses;

    public SnapshotActor() {
        this.subSystems = new LinkedList<>();
        this.responses = 0;
    }

    @Override
    public void preStart() throws Exception {
        subSystems.clear();
        responses = 0;

        context().system().eventStream().subscribe(self(), RegisterSubSystemMessage.class);

        schedule();
    }

    private void schedule() {
        getContext()
                .getSystem()
                .scheduler()
                .scheduleOnce(
                        DELAY,
                        self(),
                        new TakeSnapshotMessage(),
                        getContext().dispatcher(),
                        self()
                );
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TakeSnapshotMessage.class, this::startSnapshot)
                .match(RegisterSubSystemMessage.class, message -> subSystems.add(message.getSubSystem()))
                .match(SnapshotMessage.class, this::aggregate)
                .build();
    }

    private void aggregate(SnapshotMessage message) {
        log.info("Responses: {}, Message: {}", ++responses, message);
        if (responses == subSystems.size()) {
            schedule();
        } else if (responses > subSystems.size()) {
            log.error("Invalid number of responses. Sender: {}, Responses: {}, Subsystems: {}", sender(), responses, subSystems);
        }
    }

    private void startSnapshot(TakeSnapshotMessage message) {
        log.info("Starting a snapshot. Subsystems: {}", subSystems);

        responses = 0;
        subSystems.forEach(subSystem -> subSystem.tell(new TakeSnapshotMessage(), self()));
    }
}
