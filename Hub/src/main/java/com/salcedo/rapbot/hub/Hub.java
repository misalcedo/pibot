package com.salcedo.rapbot.hub;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.driver.DriverActor;
import com.salcedo.rapbot.driver.KeyboardDriverStrategy;
import com.salcedo.rapbot.locomotion.MotorActor;
import com.salcedo.rapbot.sense.SenseActor;
import com.salcedo.rapbot.snapshot.RegisterSubSystemMessage;
import com.salcedo.rapbot.snapshot.SnapshotRouterActor;
import com.salcedo.rapbot.snapshot.StartSnapshotMessage;
import com.salcedo.rapbot.snapshot.SystemSnapshot;
import com.salcedo.rapbot.userinterface.GraphicalUserInterface;
import com.salcedo.rapbot.userinterface.GraphicalUserInterfaceActor;
import com.salcedo.rapbot.vision.VisionActor;

import java.nio.file.Path;

public final class Hub extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(this);
    private final ServiceFactory serviceFactory;
    private final GraphicalUserInterface gui;
    private final Path workingDirectory;

    public Hub(
            final ServiceFactory serviceFactory,
            final GraphicalUserInterface gui,
            final Path workingDirectory) {
        this.serviceFactory = serviceFactory;
        this.gui = gui;
        this.workingDirectory = workingDirectory;
    }

    public static Props props(
            final ServiceFactory serviceFactory,
            final GraphicalUserInterface gui,
            final Path workingDirectory) {
        return Props.create(Hub.class, serviceFactory, gui, workingDirectory);
    }

    @Override
    public void preStart() {
        log.info("Starting Hub with working directory of: {}.", workingDirectory);

        final ActorRef motors = getContext().actorOf(MotorActor.props(serviceFactory.motor()), "motors");
        final ActorRef vision = getContext().actorOf(VisionActor.props(serviceFactory.vision()), "vision");
        final ActorRef sensors = getContext().actorOf(SenseActor.props(serviceFactory.sense()), "sensors");
        final ActorRef driver = getContext().actorOf(DriverActor.props(motors, new KeyboardDriverStrategy()), "driver");

        //getContext().actorOf(SnapshotWriterActor.props(workingDirectory), "writer");
        getContext().actorOf(GraphicalUserInterfaceActor.props(gui), "gui");

        createSnapshot();

        context().system().eventStream().publish(new RegisterSubSystemMessage(vision));
        context().system().eventStream().publish(new RegisterSubSystemMessage(sensors));
        context().system().eventStream().publish(new RegisterSubSystemMessage(driver));
        context().system().eventStream().publish(new RegisterSubSystemMessage(motors));
        context().system().eventStream().publish(new StartSnapshotMessage());
    }

    private void createSnapshot() {
        final ActorRef snapshot = getContext().actorOf(SnapshotRouterActor.props(), "snapshot");

        context().system().eventStream().subscribe(snapshot, StartSnapshotMessage.class);
        context().system().eventStream().subscribe(snapshot, RegisterSubSystemMessage.class);
        context().system().eventStream().subscribe(self(), SystemSnapshot.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Terminated.class, this::terminate)
                .match(SystemSnapshot.class, this::state)
                .build();
    }

    private void state(final SystemSnapshot snapshot) {
        getContext().getSystem().eventStream().publish(new SnapshotBackedSystemState(snapshot, context()));
    }

    private void terminate(Terminated message) {
        log.error("Actor terminated: {}. Shutting down system.", message.actor());
    }
}
