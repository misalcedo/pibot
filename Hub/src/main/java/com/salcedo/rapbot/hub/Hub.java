package com.salcedo.rapbot.hub;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.EventStream;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.model.Uri;
import com.salcedo.rapbot.driver.DriverActor;
import com.salcedo.rapbot.driver.DriverStrategy;
import com.salcedo.rapbot.driver.KeyboardDriver;
import com.salcedo.rapbot.sense.*;
import com.salcedo.rapbot.snapshot.RegisterSubSystemMessage;
import com.salcedo.rapbot.snapshot.SnapshotActor;
import com.salcedo.rapbot.userinterface.GraphicalUserInterface;
import com.salcedo.rapbot.userinterface.GraphicalUserInterfaceActor;
import org.apache.spark.sql.SQLContext;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;

public final class Hub extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Uri pi2;
    private final Uri zero;
    private final GraphicalUserInterface gui;
    private final DriverStrategy<KeyEvent> manualDriver;
    private ActorRef driver;
    private ActorRef sensors;
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
        final SenseService senseService = SenseServiceFactory.http(getContext().getSystem(), pi2.port(3002));

        driver = getContext().actorOf(DriverActor.props(pi2.port(3000), manualDriver), "driver");
        sensors = getContext().actorOf(SenseActor.props(senseService),"sensors");
        snapshot = getContext().actorOf(SnapshotActor.props(), "snapshot");
        guiUpdator = getContext().actorOf(GraphicalUserInterfaceActor.props(gui), "gui");
    }

    @Override
    public void postStop() {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .build();
    }
}
