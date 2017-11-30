package com.salcedo.rapbot.hub;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.model.Uri;
import com.salcedo.rapbot.hub.driver.KeyboardDriver;
import com.salcedo.rapbot.learner.LearningDriverActor;
import com.salcedo.rapbot.sense.Orientation;
import com.salcedo.rapbot.sense.OrientationRequest;
import com.salcedo.rapbot.sense.SenseActor;
import com.salcedo.rapbot.sense.SenseServiceFactory;
import com.salcedo.rapbot.snapshot.RegisterSubSystemMessage;
import com.salcedo.rapbot.snapshot.SnapshotActor;
import org.apache.spark.sql.SQLContext;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public final class Hub extends AbstractActor {
    private static final FiniteDuration SENSE_DELAY = Duration.create(1L, TimeUnit.SECONDS);
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Uri rpi2;
    private final SQLContext sqlContext;
    private ActorRef driver;
    private ActorRef motors;
    private ActorRef sensors;
    private ActorRef learner;
    private ActorRef snapshot;

    public Hub(final Uri rpi2, final SQLContext sqlContext) {
        this.rpi2 = rpi2;
        this.sqlContext = sqlContext;
    }

    static Props props(final Uri rpi2, final SQLContext sqlContext) {
        return Props.create(Hub.class, rpi2, sqlContext);
    }

    @Override
    public void preStart() {
        driver = getContext().actorOf(KeyboardDriver.props(rpi2.port(3000)), "driver");
        sensors = getContext().actorOf(
                SenseActor.props(SenseServiceFactory.http(getContext().getSystem(), rpi2.port(3002))),
                "sensors"
        );
        learner = getContext().actorOf(Props.create(
                LearningDriverActor.class,
                sqlContext,
                Paths.get("~", "RapBot", "orientation.parquet"),
                10
        ));
        snapshot = getContext().actorOf(SnapshotActor.props(), "snapshot");

        snapshot.tell(new RegisterSubSystemMessage(sensors), motors);
    }

    @Override
    public void postStop() throws Exception {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Orientation.class, this::logSenseResponse)
                .build();
    }

    private void logSenseResponse(final Orientation response) {
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
