package com.salcedo.rapbot.hub;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.model.Uri;
import com.salcedo.rapbot.hub.driver.KeyboardDriver;
import com.salcedo.rapbot.hub.services.MotorActor;
import com.salcedo.rapbot.motor.MotorServiceFactory;
import com.salcedo.rapbot.sense.OrientationRequest;
import com.salcedo.rapbot.sense.OrientationResponse;
import com.salcedo.rapbot.sense.SenseActor;
import com.salcedo.rapbot.sense.SenseServiceFactory;
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

    public RapBot(final Uri rpi2, SQLContext sqlContext) {
        this.rpi2 = rpi2;
        this.sqlContext = sqlContext;
    }

    @Override
    public void preStart() {
        motors = getContext().actorOf(Props.create(
                MotorActor.class,
                MotorServiceFactory.http(getContext().getSystem(), rpi2.port(3000))
        ));
        driver = getContext().actorOf(Props.create(KeyboardDriver.class, motors));
        sensors = getContext().actorOf(Props.create(
                SenseActor.class,
                SenseServiceFactory.http(getContext().getSystem(), rpi2.port(3002))
        ));
        learner = getContext().actorOf(Props.create(
                SenseActor.class,
                sqlContext,
                Paths.get("~", "RapBot", "orientation.parquet"),
                1000
        ));

        sensors.tell(new OrientationRequest(), self());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrientationResponse.class, this::logSenseResponse)
                .match(Terminated.class, this::shutdown)
                .build();
    }

    private void shutdown(final Terminated terminated) {
        log.error("Driver terminated unexpectedly. Driver: {}", terminated.actor());
        getContext().stop(self());
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
