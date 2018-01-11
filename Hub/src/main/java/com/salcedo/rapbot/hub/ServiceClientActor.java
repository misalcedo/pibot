package com.salcedo.rapbot.hub;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.CircuitBreaker;
import com.salcedo.rapbot.snapshot.ObjectSnapshotMessage;
import com.salcedo.rapbot.snapshot.TakeSnapshotMessage;
import scala.concurrent.duration.Duration;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static akka.pattern.PatternsCS.pipe;

public abstract class ServiceClientActor extends AbstractActor {
    private final CircuitBreaker breaker;

    protected ServiceClientActor() {
        this.breaker = new CircuitBreaker(
                getContext().dispatcher(),
                getContext().system().scheduler(),
                3,
                Duration.create(100, TimeUnit.MILLISECONDS),
                Duration.create(1, TimeUnit.MINUTES)
        );
    }

    protected  ReceiveBuilder baseReceiveBuilder() {
        return receiveBuilder()
                .match(TakeSnapshotMessage.class, this::snapshot);
    }

    protected <T> void pipeToSender(Callable<CompletionStage<T>> callable) {
        pipe(
                callWithBreaker(callable),
                getContext().dispatcher()
        ).to(sender());
    }

    protected <T> CompletionStage<T> callWithBreaker(Callable<CompletionStage<T>> callable) {
        return breaker.callWithCircuitBreakerCS(callable);
    }

    protected void snapshot(final TakeSnapshotMessage message) {
        final CompletionStage<ObjectSnapshotMessage> completionStage = callWithBreaker(this::snapshot)
                .thenApply(response -> new ObjectSnapshotMessage(message.getUuid(), response));
        pipe(completionStage, getContext().dispatcher()).to(sender(), self());
    }

    protected abstract CompletionStage<?> snapshot();
}
