package com.salcedo.rapbot.userinterface;

/**
 * Interface to control the GUI for RapBot.
 */
public interface GraphicalUserInterface {
    /**
     * Renders the user interface on the screen.
     */
    void display();

    void onClose(final Runnable runnable);

    void update(final SystemState state);
}
