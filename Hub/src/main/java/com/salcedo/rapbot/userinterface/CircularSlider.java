package com.salcedo.rapbot.userinterface;

import javax.swing.*;
import java.awt.*;

final class CircularSlider extends JSlider {
    CircularSlider(int min, int max) {
        super(min, max);
    }

    @Override
    public void paint(Graphics graphics) {
        graphics.drawOval(0, -10, 50, 50);
        graphics.drawOval(10, 30, 40, 40);
    }
}