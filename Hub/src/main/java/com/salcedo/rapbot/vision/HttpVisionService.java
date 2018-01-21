package com.salcedo.rapbot.vision;

import akka.http.javadsl.Http;
import akka.http.javadsl.model.*;
import akka.stream.IOResult;
import akka.stream.Materializer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static akka.stream.javadsl.FileIO.toPath;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createTempFile;

public final class HttpVisionService implements VisionService {
    private final Http http;
    private final Materializer materializer;
    private final Uri destination;
    private final Path workingDirectory;

    HttpVisionService(final Http http, final Materializer materializer, final Uri destination, final Path workingDirectory) {
        this.http = http;
        this.materializer = materializer;
        this.destination = destination;
        this.workingDirectory = workingDirectory;
    }

    @Override
    public CompletionStage<Path> takePicture() {
        try {
            final Path path = createPath();
            return sendTakePictureRequest(path);
        } catch (IOException e) {
            final CompletableFuture<Path> completableFuture = new CompletableFuture<>();
            completableFuture.completeExceptionally(e);
            return completableFuture;
        }
    }

    private CompletionStage<Path> sendTakePictureRequest(Path path) {
        return http.singleRequest(createHttpRequest(), materializer)
                .thenApply(HttpResponse::entity)
                .thenApply(HttpEntity::getDataBytes)
                .thenCompose(source -> source.runWith(toPath(path), materializer))
                .thenApply(ioResult -> mapToPath(path, ioResult));
    }

    private Path mapToPath(final Path path, final IOResult ioResult) {
        if (ioResult.wasSuccessful()) {
            return path;
        }

        throw new RuntimeException(ioResult.getError());
    }

    private HttpRequest createHttpRequest() {
        return HttpRequest.create()
                .withUri(destination)
                .withMethod(HttpMethods.GET);
    }

    private Path createPath() throws IOException {
        return createTempFile(createDirectories(workingDirectory.resolve("images")), "image", ".jpg");
    }
}
