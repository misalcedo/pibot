package com.salcedo.rapbot.driver;

import java.awt.event.KeyEvent;

public final class KeyboardDriverStrategy implements DriverStrategy<KeyEvent> {
    private static final int THROTTLE_STEP = 10;
    private static final int ORIENTATION_STEP = 15;

    @Override
    public DriveState drive(final KeyEvent keyEvent, final DriveState current) {
        if (keyEvent.getID() != KeyEvent.KEY_RELEASED) {
            return current;
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_UP) {
            return current.updateThrottle(THROTTLE_STEP);
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN) {
            return current.updateThrottle(THROTTLE_STEP * -1);
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_RIGHT) {
            return current.updateOrientation(ORIENTATION_STEP);
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_LEFT) {
            return current.updateOrientation(ORIENTATION_STEP * -1);
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_SPACE) {
            return current.updateOrientation(180);
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_B) {
            return current.minThrottle();
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_F) {
            return current.maxThrottle();
        } else {
            return current;
        }
    }
}
