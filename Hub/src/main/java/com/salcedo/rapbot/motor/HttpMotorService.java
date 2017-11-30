package com.salcedo.rapbot.motor;

import akka.http.javadsl.Http;
import akka.http.javadsl.model.*;
import akka.stream.Materializer;
import akka.util.ByteString;
import com.google.gson.Gson;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public final class HttpMotorService implements MotorService {
    private static final long TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(1L);
    private final Http http;
    private final Materializer materializer;
    private final Uri destination;
    private final Gson gson;

    HttpMotorService(final Http http, final Materializer materializer, final Uri destination, final Gson gson) {
        this.http = http;
        this.materializer = materializer;
        this.destination = destination;
        this.gson = gson;
    }

    @Override
    public CompletionStage<MotorResponse> drive(final MotorRequest request) {
        final HttpRequest httpRequest = createHttpRequest(request);

        return makeMotorRequest(httpRequest);
    }

    @Override
    public CompletionStage<MotorResponse> state() {
        final HttpRequest httpRequest = HttpRequest.create()
                .withUri(destination.addPathSegment("/motors"))
                .withMethod(HttpMethods.GET);

        return makeMotorRequest(httpRequest);
    }

    @Override
    public CompletionStage<MotorResponse> release() {
        final HttpRequest httpRequest = HttpRequest.create()
                .withUri(destination.addPathSegment("/release"))
                .withMethod(HttpMethods.PUT);

        return makeMotorRequest(httpRequest);
    }

    private CompletionStage<MotorResponse> makeMotorRequest(HttpRequest httpRequest) {
        return http.singleRequest(httpRequest, materializer)
                .thenApply(HttpResponse::entity)
                .thenCompose(this::getStrictEntity)
                .thenApply(HttpEntity.Strict::getData)
                .thenApply(ByteString::utf8String)
                .thenApply(this::buildMotorResponse);
    }

    private HttpRequest createHttpRequest(final MotorRequest request) {
        final HttpEntity.Strict entity = HttpEntities.create(ContentTypes.APPLICATION_JSON, gson.toJson(request));

        return HttpRequest.create()
                .withUri(destination.addPathSegment("/motors"))
                .withEntity(entity)
                .withMethod(HttpMethods.PUT);
    }

    private CompletionStage<HttpEntity.Strict> getStrictEntity(final ResponseEntity responseEntity) {
        return responseEntity.toStrict(TIMEOUT_MILLIS, materializer);
    }

    private MotorResponse buildMotorResponse(final String response) {
        return gson.fromJson(response, MotorResponse.class);
    }
}
