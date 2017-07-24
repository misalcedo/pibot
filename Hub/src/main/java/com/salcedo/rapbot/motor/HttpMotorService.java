package com.salcedo.rapbot.motor;

import akka.http.javadsl.Http;
import akka.http.javadsl.model.*;
import akka.http.javadsl.model.headers.ContentType;
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

    public HttpMotorService(Http http, Materializer materializer, Uri destination, Gson gson) {
        this.http = http;
        this.materializer = materializer;
        this.destination = destination;
        this.gson = gson;
    }

    @Override
    public CompletionStage<MotorResponse> drive(MotorRequest request) {
        HttpRequest httpRequest = createHttpRequest(request);

        return http.singleRequest(httpRequest, materializer)
                .thenApply(HttpResponse::entity)
                .thenCompose(this::getStrictEntity)
                .thenApply(HttpEntity.Strict::getData)
                .thenApply(ByteString::utf8String)
                .thenApply(this::buildMotorResponse);
    }

    private HttpRequest createHttpRequest(MotorRequest request) {
        final HttpEntity.Strict entity = HttpEntities.create(ContentTypes.APPLICATION_JSON, gson.toJson(request));

        return HttpRequest.create()
                .addHeader(ContentType.create(ContentTypes.APPLICATION_JSON))
                .withUri(destination)
                .withEntity(entity)
                .withMethod(HttpMethods.PUT);
    }

    private CompletionStage<HttpEntity.Strict> getStrictEntity(ResponseEntity responseEntity) {
        return responseEntity.toStrict(TIMEOUT_MILLIS, materializer);
    }

    private MotorResponse buildMotorResponse(String response) {
        return new MotorResponse();
    }
}
