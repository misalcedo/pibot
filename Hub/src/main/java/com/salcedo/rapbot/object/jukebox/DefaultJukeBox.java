package com.salcedo.rapbot.object.jukebox;

import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class DefaultJukeBox implements JukeBox {
    private final AtomicBoolean playingMusic;
    private final AtomicInteger index;
    private final List<Path> songNames;
    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private final LoggingAdapter log;

    DefaultJukeBox(final ActorSystem system, final List<Path> songNames, final EmbeddedMediaPlayerComponent mediaPlayerComponent) {
        this.songNames = songNames;
        this.mediaPlayerComponent = mediaPlayerComponent;
        this.log = Logging.getLogger(system, this);
        this.index = new AtomicInteger(-1);
        this.playingMusic = new AtomicBoolean(false);
    }

    @Override
    public void playNextSong() {
        this.log.info("Play next song. JukeBox State: {}", this);


        this.index.compareAndSet(this.songNames.size(), -1);

        final Path song = this.songNames.get(this.index.incrementAndGet());

        playSong(song);
    }

    private boolean playSong(final Path song) {
        this.log.info("Playing song {}. JukeBox State: {}", song, this);
        return this.mediaPlayerComponent.getMediaPlayer().playMedia(song.toString());
    }

    @Override
    public void playPreviousSong() {
        this.log.info("Play previous song. JukeBox State: {}", this);

        this.index.compareAndSet(0, this.songNames.size());

        final Path song = this.songNames.get(this.index.decrementAndGet());

        playSong(song);
    }

    @Override
    public boolean isPlayingMusic() {
        return this.playingMusic.get();
    }

    @Override
    public void pauseOrResume() {
        if (isPlayingMusic()) {
            pause();
        } else {
            resume();
        }
    }

    private void pause() {
        this.log.info("Paused music. JukeBox State: {}", this);
        this.mediaPlayerComponent.getMediaPlayer().pause();
        this.playingMusic.set(false);
    }

    private void resume() {
        this.log.info("Resumed music. JukeBox State: {}", this);
        this.mediaPlayerComponent.getMediaPlayer().play();
        this.playingMusic.set(true);
    }

    @Override
    public void turnOff() {
        this.mediaPlayerComponent.release();
    }

    @Override
    public String toString() {
        return "DefaultJukeBox{" +
                "playingMusic=" + this.playingMusic.get() +
                ", index=" + this.index.get() +
                ", songNames=" + this.songNames.size() +
                '}';
    }
}
