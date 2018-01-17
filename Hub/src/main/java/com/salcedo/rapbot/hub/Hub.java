package com.salcedo.rapbot.hub;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.model.Uri;
import com.salcedo.rapbot.driver.DriverActor;
import com.salcedo.rapbot.driver.DriverStrategy;
import com.salcedo.rapbot.driver.KeyboardDriverStrategy;
import com.salcedo.rapbot.learner.SnapshotWriterActor;
import com.salcedo.rapbot.locomotion.MotorActor;
import com.salcedo.rapbot.locomotion.MotorService;
import com.salcedo.rapbot.locomotion.MotorServiceFactory;
import com.salcedo.rapbot.sense.SenseActor;
import com.salcedo.rapbot.sense.SenseService;
import com.salcedo.rapbot.sense.SenseServiceFactory;
import com.salcedo.rapbot.snapshot.RegisterSubSystemMessage;
import com.salcedo.rapbot.snapshot.SnapshotRouterActor;
import com.salcedo.rapbot.snapshot.StartSnapshotMessage;
import com.salcedo.rapbot.userinterface.GraphicalUserInterface;
import com.salcedo.rapbot.userinterface.GraphicalUserInterfaceActor;
import com.salcedo.rapbot.vision.VisionActor;
import com.salcedo.rapbot.vision.VisionService;
import com.salcedo.rapbot.vision.VisionServiceFactory;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.createTempDirectory;

public final class Hub extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(this);
    private final Uri motorServiceUri;
    private final Uri visionServiceUri;
    private final Uri senseServiceUri;
    private final GraphicalUserInterface gui;
    private final DriverStrategy<KeyEvent> manualDriver;

    public Hub(
            final Uri motorServiceUri,
            final Uri visionServiceUri,
            final Uri senseServiceUri,
            final GraphicalUserInterface gui,
            final DriverStrategy<KeyEvent> manualDriver
    ) {
        this.motorServiceUri = motorServiceUri;
        this.visionServiceUri = visionServiceUri;
        this.senseServiceUri = senseServiceUri;
        this.gui = gui;
        this.manualDriver = manualDriver;
    }

    public static Props props(
            final Uri motorServiceUri,
            final Uri visionServiceUri,
            final Uri senseServiceUri,
            final GraphicalUserInterface gui
    ) {
        return Props.create(
                Hub.class,
                motorServiceUri,
                visionServiceUri,
                senseServiceUri,
                gui,
                new KeyboardDriverStrategy()
        );
    }

    @Override
    public void preStart() throws Exception {
        final MotorService motorService = MotorServiceFactory.http(getContext().getSystem(), motorServiceUri);
        final VisionService visionService = VisionServiceFactory.http(getContext().getSystem(), visionServiceUri);
        final SenseService senseService = SenseServiceFactory.http(getContext().getSystem(), senseServiceUri);

        final ActorRef motors = getContext().actorOf(MotorActor.props(motorService), "motors");
        final ActorRef vision = getContext().actorOf(VisionActor.props(visionService), "vision");
        final ActorRef sensors = getContext().actorOf(SenseActor.props(senseService), "sensors");
        final ActorRef driver = getContext().actorOf(DriverActor.props(motors, manualDriver), "driver");
        getContext().actorOf(SnapshotWriterActor.props(createWriterDirectory()), "writer");
        getContext().actorOf(GraphicalUserInterfaceActor.props(this.gui), "gui");

        createSnapshot();

        context().system().eventStream().publish(new RegisterSubSystemMessage(vision));
        context().system().eventStream().publish(new RegisterSubSystemMessage(sensors));
        context().system().eventStream().publish(new RegisterSubSystemMessage(driver));
        context().system().eventStream().publish(new RegisterSubSystemMessage(motors));
        context().system().eventStream().publish(new StartSnapshotMessage());
    }

    private Path createWriterDirectory() throws IOException {
        return createTempDirectory(getContext().getSystem().name());
    }

    private void createSnapshot() {
        final ActorRef snapshot = getContext().actorOf(SnapshotRouterActor.props(), "snapshot");

        context().system().eventStream().subscribe(snapshot, StartSnapshotMessage.class);
        context().system().eventStream().subscribe(snapshot, RegisterSubSystemMessage.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Terminated.class, this::terminate)
                .build();
    }

    private void terminate(Terminated message) {
        log.error("Actor terminated: {}. Shutting down system.", message.actor());
    }
}
