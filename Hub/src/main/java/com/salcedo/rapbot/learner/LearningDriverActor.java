package com.salcedo.rapbot.learner;

import akka.actor.AbstractActor;
import com.salcedo.rapbot.sense.OrientationResponse;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static org.apache.spark.sql.SaveMode.Append;

public class LearningDriverActor extends AbstractActor {
    private final SQLContext sqlContext;
    private final Path path;
    private final int bufferSize;
    private final List<OrientationResponse> buffer;

    public LearningDriverActor(final SQLContext sqlContext, final Path path, final int bufferSize) {
        this.sqlContext = sqlContext;
        this.path = path;
        this.bufferSize = bufferSize;
        this.buffer = new LinkedList<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrientationResponse.class, this::writeOrientation)
                .build();
    }

    private void writeOrientation(OrientationResponse response) {
        buffer.add(response);
        if (buffer.size() >= bufferSize) {
            flush();
        }
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        buffer.clear();
    }

    @Override
    public void postStop() throws Exception {
        super.postStop();
        flush();
    }

    private void flush() {
        final Dataset<Row> orientations = sqlContext.createDataFrame(buffer, OrientationResponse.class);
        orientations.write().mode(Append).save();
    }
}
