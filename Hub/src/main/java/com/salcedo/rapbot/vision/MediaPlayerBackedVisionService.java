package com.salcedo.rapbot.vision;

import akka.actor.ActorSystem;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class MediaPlayerBackedVisionService extends MediaPlayerEventAdapter implements VisionService {
    private final Map<String, CompletableFuture<Path>> requests;
    private final MediaPlayer embeddedMediaPlayer;

    MediaPlayerBackedVisionService(MediaPlayer embeddedMediaPlayer) {
        this.requests = new ConcurrentHashMap<>();
        this.embeddedMediaPlayer = embeddedMediaPlayer;
    }

    @Override
    public CompletionStage<Path> takePicture() {
        embeddedMediaPlayer.addMediaPlayerEventListener(this);

        final Path path = createPath();

        requests.put(path.toAbsolutePath().toString(), new CompletableFuture<>());

        embeddedMediaPlayer.saveSnapshot(path.toAbsolutePath().toFile());

        return completedFuture(path);
    }

    private Path createPath() {
        try {
            return Files.createTempFile("image", ".jpg");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void snapshotTaken(MediaPlayer mediaPlayer, String filename) {
        requests.remove(filename).complete(Paths.get(filename));
    }
}
