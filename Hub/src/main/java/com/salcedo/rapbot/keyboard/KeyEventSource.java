package com.salcedo.rapbot.keyboard;

import akka.actor.ActorRef;

import javax.swing.*;
import java.awt.*;

public class KeyEventSource implements EventSource {
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    @Override
    public void listen(ActorRef actor) {
        //Create and set up the window.
        JFrame frame = new JFrame("KeyEvent Listener");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextField typingArea = new JTextField(20);
        typingArea.addKeyListener(new KeyEventForwarder(actor));

        JTextArea displayArea = new JTextArea();
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setPreferredSize(new Dimension(375, 125));

        frame.getContentPane().add(typingArea, BorderLayout.PAGE_START);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
}
