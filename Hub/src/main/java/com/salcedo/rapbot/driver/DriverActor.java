package com.salcedo.rapbot.driver;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.snapshot.ObjectSnapshotMessage;
import com.salcedo.rapbot.snapshot.StartSnapshotMessage;
import com.salcedo.rapbot.snapshot.TakeSnapshotMessage;

import java.awt.event.KeyEvent;

public final class DriverActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final DriverStrategy<KeyEvent> driverStrategy;
    private final DriverMotorStrategy motorStrategy;
    private final ActorRef motors;
    private DriveState desiredState;

    public DriverActor(
            final ActorRef motors,
            final DriverStrategy<KeyEvent> driverStrategy
    ) {
        this.motors = motors;
        this.driverStrategy = driverStrategy;
        this.motorStrategy = new CosineDriverMotorStrategy();
        this.desiredState = new DriveState(
                new OpenClosedRange(0, 255),
                new OpenClosedRange(0, 360),
                0,
                90
        );
    }

    public static Props props(
            final ActorRef motors,
            final DriverStrategy<KeyEvent> driverStrategy
    ) {
        return Props.create(DriverActor.class, motors, driverStrategy);
    }

    @Override
    public void preStart() {
        context().system().eventStream().subscribe(self(), KeyEvent.class);

        context().watch(motors);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(KeyEvent.class, this::drive)
                .match(TakeSnapshotMessage.class, this::snapshot)
                .match(Terminated.class, message -> terminate())
                .build();
    }

    private void terminate() {
        context().stop(self());
    }

    private void snapshot(TakeSnapshotMessage message) {
        sender().tell(new ObjectSnapshotMessage(message.getUuid(), desiredState), self());
    }

    private void drive(final KeyEvent keyEvent) {
        desiredState = driverStrategy.drive(keyEvent, desiredState);

        log.debug("Changed desired drive state to {}", desiredState);

        motors.tell(motorStrategy.drive(desiredState), self());

        context().system().eventStream().publish(new StartSnapshotMessage());
    }
}
