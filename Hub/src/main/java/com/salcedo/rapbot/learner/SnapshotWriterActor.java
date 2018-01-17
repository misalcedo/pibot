package com.salcedo.rapbot.learner;

import akka.actor.AbstractActor;
import akka.actor.ActorPath;
import akka.actor.ActorPaths;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.sense.EnvironmentReading;
import com.salcedo.rapbot.sense.ThreeDimensionalSensorReading;
import com.salcedo.rapbot.snapshot.SystemSnapshot;
import kamon.Kamon;
import kamon.metric.GaugeMetric;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonMap;
import static org.apache.spark.sql.SaveMode.Append;

public class SnapshotWriterActor extends AbstractActor {
    private static final double GRAVITY_ACCELERATION = 9.81;
    public static final int TO_CENTIMETERS = 100;
    private final LoggingAdapter log = Logging.getLogger(this);
    private final SQLContext sqlContext;
    private final Path path;
    private final long bufferSize;
    private final List<SystemSnapshot> buffer;

    public SnapshotWriterActor(final Path path) {
        final SparkConf sparkConf = getSparkConf();
        this.sqlContext = SQLContext.getOrCreate(SparkContext.getOrCreate(sparkConf));
        this.bufferSize = 1;
        this.buffer = new LinkedList<>();
        this.path = path;
    }

    private SparkConf getSparkConf() {
        final SparkConf sparkConf = new SparkConf();

        sparkConf.setAppName("RapBot");
        sparkConf.setMaster("local[*]");

        return sparkConf;
    }

    public static Props props(final Path path) {
        return Props.create(SnapshotWriterActor.class, path);
    }

    private void buffer(final SystemSnapshot systemSnapshot) {
        final GaugeMetric acceleration = Kamon.gauge("accelerationInCentimetersPerSecond");
        final ActorPath sense = ActorPaths.fromString("akka://" + getContext().getSystem().name() + "/user/hub/sensors");
        final Optional<ThreeDimensionalSensorReading> snapshot = systemSnapshot.getSnapshot(sense, EnvironmentReading.class)
                .map(EnvironmentReading::getAccelerometer);

        snapshot.map(ThreeDimensionalSensorReading::getX)
                .ifPresent(x -> acceleration.refine(singletonMap("dimension", "x")).set((long) (TO_CENTIMETERS * x * GRAVITY_ACCELERATION)));
        snapshot.map(ThreeDimensionalSensorReading::getY)
                .ifPresent(x -> acceleration.refine(singletonMap("dimension", "y")).set((long) (TO_CENTIMETERS * x * GRAVITY_ACCELERATION)));
        snapshot.map(ThreeDimensionalSensorReading::getZ)
                .ifPresent(x -> acceleration.refine(singletonMap("dimension", "z")).set((long) (TO_CENTIMETERS * x * GRAVITY_ACCELERATION)));

        buffer.add(systemSnapshot);
        if (buffer.size() >= bufferSize) {
            flush();
        }
    }

    private void flush() {
        //log.info("Flushing learning orientation.");

        //write();
    }

    private void write() {
        final Dataset<Row> snapshots = sqlContext.createDataFrame(buffer, SystemSnapshot.class);
        snapshots.write().mode(Append).save(path.toAbsolutePath().toString());
    }

    @Override
    public void preStart() {
        buffer.clear();
        getContext().getSystem().eventStream().subscribe(self(), SystemSnapshot.class);
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        flush();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SystemSnapshot.class, this::buffer)
                .build();
    }
}
