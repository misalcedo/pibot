package com.salcedo.rapbot.hub;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.model.Uri;
import com.salcedo.rapbot.hub.driver.KeyboardDriver;
import com.salcedo.rapbot.motor.MotorActor;
import com.salcedo.rapbot.learner.LearningDriverActor;
import com.salcedo.rapbot.motor.MotorServiceFactory;
import com.salcedo.rapbot.sense.OrientationRequest;
import com.salcedo.rapbot.sense.OrientationResponse;
import com.salcedo.rapbot.sense.SenseActor;
import com.salcedo.rapbot.sense.SenseServiceFactory;
import com.salcedo.rapbot.snapshot.RegisterSubSystemMessage;
import com.salcedo.rapbot.snapshot.SnapshotActor;
import org.apache.spark.sql.SQLContext;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public final class RapBot extends AbstractActor {
    private static final FiniteDuration SENSE_DELAY = Duration.create(1L, TimeUnit.SECONDS);
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Uri rpi2;
    private final SQLContext sqlContext;
    private ActorRef driver;
    private ActorRef motors;
    private ActorRef sensors;
    private ActorRef learner;
    private ActorRef snapshot;

    public RapBot(final Uri rpi2, SQLContext sqlContext) {
        this.rpi2 = rpi2;
        this.sqlContext = sqlContext;
    }

    @Override
    public void preStart() {
        driver = getContext().actorOf(Props.create(KeyboardDriver.class, rpi2.port(3000)));
        sensors = getContext().actorOf(Props.create(
                SenseActor.class,
                SenseServiceFactory.http(getContext().getSystem(), rpi2.port(3002))
        ));
        learner = getContext().actorOf(Props.create(
                LearningDriverActor.class,
                sqlContext,
                Paths.get("~", "RapBot", "orientation.parquet"),
                10
        ));
        snapshot = getContext().actorOf(Props.create(SnapshotActor.class));

        snapshot.tell(new RegisterSubSystemMessage(sensors), motors);
    }

    @Override
    public void postStop() throws Exception {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrientationResponse.class, this::logSenseResponse)
                .build();
    }

    private void logSenseResponse(final OrientationResponse response) {
        getContext()
                .getSystem()
                .scheduler()
                .scheduleOnce(
                        SENSE_DELAY,
                        sensors,
                        new OrientationRequest(),
                        getContext().dispatcher(),
                        self()
                );

        log.info("{}", response);

        learner.tell(response, self());
    }
}
