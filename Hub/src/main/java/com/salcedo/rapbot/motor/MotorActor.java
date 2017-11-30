package com.salcedo.rapbot.motor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.snapshot.RegisterSubSystemMessage;
import com.salcedo.rapbot.snapshot.SnapshotMessage;
import com.salcedo.rapbot.snapshot.TakeSnapshotMessage;

public final class MotorActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final MotorService motorService;

    public MotorActor(final MotorService motorService) {
        this.motorService = motorService;
    }

    public static Props props(final MotorService motorService) {
        return Props.create(MotorActor.class, motorService);
    }

    @Override
    public void preStart() throws Exception {
        release();
        context().system().eventStream().publish(new RegisterSubSystemMessage(self()));
        context().system().eventStream().subscribe(self(), TakeSnapshotMessage.class);
    }

    @Override
    public void postStop() throws Exception {
        release();
    }

    private void release() {
        motorService.release();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(MotorRequest.class, this::drive)
                .match(TakeSnapshotMessage.class, this::snapshot)
                .build();
    }

    private void snapshot(TakeSnapshotMessage message) {
        final ActorRef sender = sender();
        motorService.state()
                .thenAccept(response -> sender.tell(new SnapshotMessage(message.getUuid(), response), self()));
    }

    private void drive(final MotorRequest request) {
        final ActorRef sender = sender();
        motorService.drive(request).thenAccept(response -> self().tell(response, sender));
    }
}
