package com.salcedo.rapbot.vision;

import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createTempFile;

public final class MediaPlayerBackedVisionService extends MediaPlayerEventAdapter implements VisionService {
    private final Map<String, CompletableFuture<Path>> requests;
    private final MediaPlayer embeddedMediaPlayer;
    private final Path workingDirectory;

    MediaPlayerBackedVisionService(final MediaPlayer embeddedMediaPlayer, final Path workingDirectory) {
        this.embeddedMediaPlayer = embeddedMediaPlayer;
        this.workingDirectory = workingDirectory;
        this.requests = new ConcurrentHashMap<>();
    }

    @Override
    public CompletionStage<Path> takePicture() {
        final CompletableFuture<Path> completableFuture = new CompletableFuture<>();

        try {
            final Path path = createPath();

            requests.put(path.toAbsolutePath().toString(), completableFuture);
            embeddedMediaPlayer.saveSnapshot(path.toAbsolutePath().toFile());
        } catch (IOException e) {
            completableFuture.completeExceptionally(e);
        }

        return completableFuture;
    }

    private Path createPath() throws IOException {
        return createTempFile(createDirectories(workingDirectory.resolve("images")), "image", ".png");
    }

    @Override
    public void snapshotTaken(final MediaPlayer mediaPlayer, final String filename) {
        requests.remove(filename).complete(Paths.get(filename));
    }
}
