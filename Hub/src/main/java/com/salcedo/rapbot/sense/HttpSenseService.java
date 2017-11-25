package com.salcedo.rapbot.sense;

import akka.http.javadsl.Http;
import akka.http.javadsl.model.*;
import akka.stream.Materializer;
import akka.util.ByteString;
import com.google.gson.Gson;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class HttpSenseService implements SenseService {
    private static final long TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(1L);
    private final Http http;
    private final Materializer materializer;
    private final Uri destination;
    private final Gson gson;

    public HttpSenseService(final Http http, final Materializer materializer, final Uri destination, final Gson gson) {
        this.http = http;
        this.materializer = materializer;
        this.destination = destination;
        this.gson = gson;
    }

    private HttpRequest createHttpRequest() {
        return HttpRequest.create()
                .withUri(destination.addPathSegment("/orientation"))
                .withMethod(HttpMethods.GET);
    }

    @Override
    public CompletionStage<OrientationResponse> getOrientation() {
        final HttpRequest httpRequest = createHttpRequest();

        return http.singleRequest(httpRequest, materializer)
                .thenApply(HttpResponse::entity)
                .thenCompose(this::getStrictEntity)
                .thenApply(HttpEntity.Strict::getData)
                .thenApply(ByteString::utf8String)
                .thenApply(this::buildResponse);
    }

    private OrientationResponse buildResponse(final String response) {
        return gson.fromJson(response, OrientationResponse.class);
    }

    private CompletionStage<HttpEntity.Strict> getStrictEntity(final ResponseEntity responseEntity) {
        return responseEntity.toStrict(TIMEOUT_MILLIS, materializer);
    }
}
