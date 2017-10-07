package com.salcedo.rapbot.vision;

import akka.http.javadsl.Http;
import akka.http.javadsl.model.*;
import akka.stream.IOResult;
import akka.stream.Materializer;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.RunnableGraph;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class HttpVisionService implements VisionService {
    private final Http http;
    private final Materializer materializer;
    private final Uri destination;

    HttpVisionService(final Http http, final Materializer materializer, final Uri destination) {
        this.http = http;
        this.materializer = materializer;
        this.destination = destination;
    }

    @Override
    public CompletionStage<Path> takePicture() {
        final HttpRequest httpRequest = createHttpRequest();

        return this.http.singleRequest(httpRequest, this.materializer)
                .thenApply(HttpResponse::entity)
                .thenCompose(this::writeEntityToFile);

    }

    private HttpRequest createHttpRequest() {
        return HttpRequest.create()
                .withUri(this.destination)
                .withMethod(HttpMethods.PUT);
    }

    private CompletionStage<Path> writeEntityToFile(final ResponseEntity responseEntity) {
        final Path path = Paths.get("~", "Desktop", "image.jpg");
        final RunnableGraph<Object> runnableGraph = responseEntity.getDataBytes()
                .to(FileIO.toPath(path));
        return CompletableFuture.supplyAsync(() -> runnableGraph.run(this.materializer))
                .thenApply(IOResult.class::cast)
                .thenApply(result -> path);
    }
}
