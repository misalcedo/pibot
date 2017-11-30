package com.salcedo.rapbot.sense;

import akka.http.javadsl.Http;
import akka.http.javadsl.model.*;
import akka.stream.Materializer;
import akka.util.ByteString;
import com.google.gson.Gson;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class HttpSenseService implements SenseService {
    private static final long TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(1L);
    private final Http http;
    private final Materializer materializer;
    private final Uri destination;
    private final Gson gson;

    HttpSenseService(final Http http, final Materializer materializer, final Uri destination, final Gson gson) {
        this.http = http;
        this.materializer = materializer;
        this.destination = destination;
        this.gson = gson;
    }

    private HttpRequest createHttpRequest(final String path) {
        return HttpRequest.create()
                .withUri(destination.addPathSegment(path))
                .withMethod(HttpMethods.GET);
    }

    @Override
    public CompletionStage<EnvironmentReading> senseEnvironment() {
        return getSensorReading("/", responseFactory(EnvironmentReading.class));
    }

    @Override
    public CompletionStage<Orientation> getOrientation() {
        return getSensorReading("/orientation", responseFactory(Orientation.class));
    }

    @Override
    public CompletionStage<ThreeDimensionalSensorReading> getAcceleration() {
        return getSensorReading("/acceleration", responseFactory(ThreeDimensionalSensorReading.class));
    }

    private <T> CompletionStage<T> getSensorReading(
            final String path,
            final Function<String, T> responseFactory
    ) {
        final HttpRequest httpRequest = createHttpRequest(path);

        return http.singleRequest(httpRequest, materializer)
                .thenApply(HttpResponse::entity)
                .thenCompose(this::getStrictEntity)
                .thenApply(HttpEntity.Strict::getData)
                .thenApply(ByteString::utf8String)
                .thenApply(responseFactory);
    }

    private <T> Function<String, T> responseFactory(Class<? extends T> type) {
        return data -> gson.fromJson(data, type);
    }

    private CompletionStage<HttpEntity.Strict> getStrictEntity(final ResponseEntity responseEntity) {
        return responseEntity.toStrict(TIMEOUT_MILLIS, materializer);
    }
}
