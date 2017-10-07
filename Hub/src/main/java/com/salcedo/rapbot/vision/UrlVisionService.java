package com.salcedo.rapbot.vision;

import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class UrlVisionService implements VisionService {
    private final LoggingAdapter log;
    private final URL url;

    UrlVisionService(final URL url, final ActorSystem system) {
        this.url = url;
        this.log = Logging.getLogger(system, this);
    }

    @Override
    public CompletionStage<Path> takePicture() {
        return CompletableFuture.supplyAsync(this::writePictureToFileUnchecked);
    }

    private Path writePictureToFileUnchecked() {
        try {
            return writePictureToFile();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path writePictureToFile() throws IOException {
        try (final InputStream inputStream = this.url.openStream()) {
            final Path path = createPath();
            final long bytesCopied = Files.copy(new BufferedInputStream(inputStream), path, REPLACE_EXISTING);

            this.log.info("Copied {} bytes to {}", bytesCopied, path.toAbsolutePath());

            return path;
        }
    }

    private Path createPath() throws IOException {
        return Files.createTempFile("image", ".jpg");
    }
}
