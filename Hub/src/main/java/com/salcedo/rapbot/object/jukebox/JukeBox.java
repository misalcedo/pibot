package com.salcedo.rapbot.object.jukebox;

public interface JukeBox {
    void playNextSong();

    void pauseOrResume();

    void playPreviousSong();

    boolean isPlayingMusic();

    void turnOff();
}
