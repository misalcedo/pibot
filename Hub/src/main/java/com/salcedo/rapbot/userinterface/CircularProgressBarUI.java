package com.salcedo.rapbot.userinterface;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import static java.lang.Math.max;
import static java.lang.Math.min;

final class CircularProgressBarUI extends BasicProgressBarUI {
    private static final int DEGREE_BUFFER = 3;
    private static final int DEGREES = 360;

    @Override
    public Dimension getPreferredSize(final JComponent component) {
        final Dimension dimension = super.getPreferredSize(component);
        final int largest = max(dimension.width, dimension.height);

        dimension.setSize(largest, largest);

        return dimension;
    }

    @Override public void paint(Graphics graphics, JComponent component) {
        final Insets insets = progressBar.getInsets(); // area for border
        final int insetsWidth = insets.right - insets.left;
        final int insetsHeight = insets.top - insets.bottom;

        final int barRectWidth  = progressBar.getWidth() - insetsWidth;
        final int barRectHeight = progressBar.getHeight() - insetsHeight;
        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return;
        }

        final Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final double degree = DEGREES * progressBar.getPercentComplete();
        final double size = min(barRectWidth, barRectHeight);
        final double centerX = insets.left + barRectWidth  * .5;
        final double centerY = insets.top  + barRectHeight * .5;
        final double outerRadius = size * .5;
        final double innerRadius = outerRadius * .5;
        final Shape inner  = createEllipse(centerX, centerY, innerRadius);
        final Shape outer  = createEllipse(centerX, centerY, outerRadius);
        final Shape sector = createArc(degree, centerX, centerY, outerRadius);

        final Area foreground = new Area(sector);
        final Area background = new Area(outer);
        final Area hole = new Area(inner);

        foreground.subtract(hole);
        background.subtract(hole);

        // draw the track
        graphics2D.setPaint(new Color(0xDDDDDD));
        graphics2D.fill(background);

        // draw the circular sector
        graphics2D.setPaint(progressBar.getForeground());
        graphics2D.fill(foreground);
        graphics2D.dispose();

        // Deal with possible text painting
        if (progressBar.isStringPainted()) {
            paintString(graphics, insets.left, insets.top, barRectWidth, barRectHeight, 0, insets);
        }
    }

    private Arc2D.Double createArc(double degree, double centerX, double centerY, double radius) {
        final double diameter = radius * 2;
        return new Arc2D.Double(
                centerX - radius,
                centerY - radius,
                diameter,
                diameter,
                degree - DEGREE_BUFFER,
                DEGREE_BUFFER * 2,
                Arc2D.PIE
        );
    }

    private Ellipse2D.Double createEllipse(double centerX, double centerY, double radius) {
        final double diameter = radius * 2;
        return new Ellipse2D.Double(centerX - radius, centerY - radius, diameter, diameter);
    }
}