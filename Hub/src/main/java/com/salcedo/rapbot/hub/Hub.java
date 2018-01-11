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
import com.salcedo.rapbot.locomotion.MotorActor;
import com.salcedo.rapbot.locomotion.MotorService;
import com.salcedo.rapbot.locomotion.MotorServiceFactory;
import com.salcedo.rapbot.sense.*;
import com.salcedo.rapbot.snapshot.RegisterSubSystemMessage;
import com.salcedo.rapbot.snapshot.SnapshotActor;
import com.salcedo.rapbot.snapshot.SnapshotRouterActor;
import com.salcedo.rapbot.snapshot.StartSnapshotMessage;
import com.salcedo.rapbot.userinterface.GraphicalUserInterface;
import com.salcedo.rapbot.userinterface.GraphicalUserInterfaceActor;
import com.salcedo.rapbot.vision.VisionActor;
import com.salcedo.rapbot.vision.VisionService;
import com.salcedo.rapbot.vision.VisionServiceFactory;

import java.awt.event.KeyEvent;

public final class Hub extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Uri pi2;
    private final Uri zero;
    private final GraphicalUserInterface gui;
    private final DriverStrategy<KeyEvent> manualDriver;
    private ActorRef driver;
    private ActorRef motors;
    private ActorRef sensors;
    private ActorRef vision;
    private ActorRef snapshot;
    private ActorRef guiUpdator;

    public Hub(
            final Uri pi2,
            final Uri zero,
            final GraphicalUserInterface gui,
            final DriverStrategy<KeyEvent> manualDriver) {
        this.pi2 = pi2;
        this.zero = zero;
        this.gui = gui;
        this.manualDriver = manualDriver;
    }

    public static Props props(final Uri pi2,
                              final Uri zero,
                              final GraphicalUserInterface gui,
                              final DriverStrategy<KeyEvent> manualDriver) {
        return Props.create(Hub.class, pi2, zero, gui, manualDriver);
    }

    @Override
    public void preStart() {
        final MotorService motorService = MotorServiceFactory.http(getContext().getSystem(), pi2.port(3000));
        final VisionService visionService = VisionServiceFactory.http(getContext().getSystem(), pi2.port(3001));
        final SenseService senseService = SenseServiceFactory.http(getContext().getSystem(), pi2.port(3002));

        motors = getContext().actorOf(MotorActor.props(motorService), "motors");
        vision = getContext().actorOf(VisionActor.props(visionService),"vision");
        sensors = getContext().actorOf(SenseActor.props(senseService),"sensors");
        driver = getContext().actorOf(DriverActor.props(motors, manualDriver), "driver");
        guiUpdator = getContext().actorOf(GraphicalUserInterfaceActor.props(gui), "gui");

        createSnapshot();
    }

    private void createSnapshot() {
        snapshot = getContext().actorOf(SnapshotRouterActor.props(), "snapshot");

        context().system().eventStream().subscribe(snapshot, StartSnapshotMessage.class);
        context().system().eventStream().subscribe(snapshot, RegisterSubSystemMessage.class);

        context().system().eventStream().publish(new RegisterSubSystemMessage(vision));
        context().system().eventStream().publish(new RegisterSubSystemMessage(sensors));
        context().system().eventStream().publish(new RegisterSubSystemMessage(driver));
        context().system().eventStream().publish(new RegisterSubSystemMessage(motors));

        context().system().eventStream().publish(new StartSnapshotMessage());
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
