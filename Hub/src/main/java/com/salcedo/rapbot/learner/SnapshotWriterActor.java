package com.salcedo.rapbot.learner;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.snapshot.SingleResponseSystemSnapshot;
import com.salcedo.rapbot.snapshot.SystemSnapshot;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static org.apache.spark.sql.SaveMode.Append;

public class SnapshotWriterActor extends AbstractActor {
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
        buffer.add(systemSnapshot);
        if (buffer.size() >= bufferSize) {
            flush();
        }
    }

    private void flush() {
        log.info("Flushing learning orientation");

        final Dataset<Row> snapshots = sqlContext.createDataFrame(buffer, SingleResponseSystemSnapshot.class);
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
