package com.salcedo.rapbot.hub;

import akka.actor.AbstractActor;
import akka.actor.Status;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.CircuitBreaker;
import com.salcedo.rapbot.snapshot.ObjectSnapshotMessage;
import com.salcedo.rapbot.snapshot.SnapshotActor;
import com.salcedo.rapbot.snapshot.SnapshotActor.TakeSubSystemSnapshot;
import com.salcedo.rapbot.snapshot.SnapshotMessage;
import com.salcedo.rapbot.snapshot.TakeSnapshotMessage;
import scala.concurrent.duration.Duration;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static akka.pattern.PatternsCS.pipe;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public abstract class ServiceClientActor extends AbstractActor {
    private final CircuitBreaker breaker;
    private CompletableFuture<?> snapshotRequest;

    protected ServiceClientActor() {
        this.breaker = new CircuitBreaker(
                getContext().dispatcher(),
                getContext().system().scheduler(),
                10,
                Duration.create(100, MILLISECONDS),
                Duration.create(1, SECONDS)
        );
        this.snapshotRequest = completedFuture(null);
    }

    protected ReceiveBuilder baseReceiveBuilder() {
        return receiveBuilder()
                .match(TakeSubSystemSnapshot.class, this::snapshot);
    }

    protected void snapshot(final TakeSubSystemSnapshot message) {
        if (snapshotRequest.isDone()) {
            snapshotRequest = callWithBreaker(this::snapshot).toCompletableFuture();
        }

        final CompletionStage<Status.Success> snapshotStage = snapshotRequest
                .thenApply(Status.Success::new);
        pipe(snapshotStage, getContext().dispatcher()).to(sender(), self());
    }

    protected <T> CompletionStage<T> callWithBreaker(Callable<CompletionStage<T>> callable) {
        return breaker.callWithCircuitBreakerCS(callable);
    }

    protected <T> void pipeToSender(Callable<CompletionStage<T>> callable) {
        pipe(callWithBreaker(callable), getContext().dispatcher()).to(sender(), self());
    }

    protected abstract CompletionStage<?> snapshot();
}
