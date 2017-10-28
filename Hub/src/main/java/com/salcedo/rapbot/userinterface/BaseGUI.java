package com.salcedo.rapbot.userinterface;

import javax.swing.*;
import java.awt.event.KeyListener;

import static javax.swing.JFrame.EXIT_ON_CLOSE;

public abstract class BaseGUI implements GraphicalUserInterface {
    private final JFrame frame;
    private final KeyListener keyListener;

    BaseGUI(final KeyListener keyListener) {
        this.frame = new JFrame("RapBot");
        this.keyListener = keyListener;
    }

    @Override
    public void display() {
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setBounds(100, 100, 640, 480);
        frame.addKeyListener(keyListener);
        frame.setVisible(true);

        initializeFrame(frame);

    }

    protected abstract void initializeFrame(final JFrame frame);

    KeyListener getKeyListener() {
        return keyListener;
    }
}
