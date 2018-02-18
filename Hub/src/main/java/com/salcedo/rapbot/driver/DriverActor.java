package com.salcedo.rapbot.driver;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.snapshot.ObjectSnapshotMessage;
import com.salcedo.rapbot.snapshot.SnapshotActor;
import com.salcedo.rapbot.snapshot.SnapshotActor.TakeSubSystemSnapshot;
import com.salcedo.rapbot.snapshot.SnapshotTakerActor.TakeSnapshot;
import com.salcedo.rapbot.snapshot.TakeSnapshotMessage;

import java.awt.event.KeyEvent;

public final class DriverActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(this);
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

    public static Props props(final ActorRef motors) {
        return Props.create(DriverActor.class, motors, new KeyboardDriverStrategy());
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
                .match(TakeSubSystemSnapshot.class, m -> this.snapshot())
                .match(Terminated.class, message -> terminate())
                .build();
    }

    private void terminate() {
        context().stop(self());
    }

    private void snapshot() {
        sender().tell(new Status.Success(desiredState), self());
    }

    private void drive(final KeyEvent keyEvent) {
        desiredState = driverStrategy.drive(keyEvent, desiredState);
        motors.tell(motorStrategy.drive(desiredState), self());
        context().system().eventStream().publish(new TakeSnapshot());

        log.debug("Changed desired drive state to {}", desiredState);
    }
}
