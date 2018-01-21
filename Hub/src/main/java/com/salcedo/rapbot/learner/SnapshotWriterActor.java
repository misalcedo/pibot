package com.salcedo.rapbot.learner;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.salcedo.rapbot.snapshot.SystemSnapshot;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SQLContext;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static org.apache.spark.sql.Encoders.kryo;
import static org.apache.spark.sql.SaveMode.Append;

public class SnapshotWriterActor extends AbstractActor {
    private final SQLContext sqlContext;
    private final Path path;
    private final long bufferSize;
    private final List<SystemSnapshot> buffer;

    public SnapshotWriterActor(final Path path) {
        final SparkConf sparkConf = getSparkConf();
        this.sqlContext = SQLContext.getOrCreate(SparkContext.getOrCreate(sparkConf));
        this.bufferSize = 100;
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
        buffer.add(systemSnapshot);
        if (buffer.size() >= bufferSize) {
            flush();
        }
    }

    private void flush() {
        sqlContext.createDataset(buffer, kryo(SystemSnapshot.class))
                .write()
                .mode(Append)
                .format("parquet")
                .save(path.resolve("snapshots.parquet").toAbsolutePath().toString());
    }

    @Override
    public void preStart() {
        buffer.clear();
        getContext().getSystem().registerOnTermination(sqlContext.sparkSession()::stop);
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
