package com.salcedo.rapbot.jukebox;

public interface JukeBox {
    void playNextSong();

    void pauseOrResume();

    void playPreviousSong();

    boolean isPlayingMusic();

    void turnOff();
}
