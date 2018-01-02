package com.salcedo.rapbot.hub;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.EventStream;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.model.Uri;
import com.salcedo.rapbot.driver.DriverStrategy;
import com.salcedo.rapbot.driver.KeyboardDriver;
import com.salcedo.rapbot.sense.*;
import com.salcedo.rapbot.snapshot.RegisterSubSystemMessage;
import com.salcedo.rapbot.snapshot.SnapshotActor;
import com.salcedo.rapbot.userinterface.GraphicalUserInterface;
import org.apache.spark.sql.SQLContext;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public final class Hub extends AbstractActor {
    private static final FiniteDuration SENSE_DELAY = Duration.create(1L, TimeUnit.SECONDS);
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Uri pi2;
    private final Uri zero;
    private final GraphicalUserInterface gui;
    private final DriverStrategy manualDriver;
    private ActorRef driver;
    private ActorRef sensors;
    private ActorRef snapshot;

    public Hub(
            final Uri pi2,
            final Uri zero,
            final GraphicalUserInterface gui,
            final DriverStrategy manualDriver) {
        this.pi2 = pi2;
        this.zero = zero;
        this.gui = gui;
        this.manualDriver = manualDriver;
    }

    public static Props props(final Uri pi2,
                              final Uri zero,
                              final GraphicalUserInterface gui,
                              final DriverStrategy manualDriver) {
        return Props.create(Hub.class, pi2, zero, gui, manualDriver);
    }

    @Override
    public void preStart() {
        final SenseService senseService = SenseServiceFactory.http(getContext().getSystem(), pi2.port(3002));
        final EventStream eventStream = getContext().getSystem().eventStream();

        driver = getContext().actorOf(KeyboardDriver.props(pi2.port(3000)), "driver");
        sensors = getContext().actorOf(SenseActor.props(senseService),"sensors");
        snapshot = getContext().actorOf(SnapshotActor.props(), "snapshot");

        eventStream.publish(new RegisterSubSystemMessage(sensors));
    }

    @Override
    public void postStop() throws Exception {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .build();
    }
}
