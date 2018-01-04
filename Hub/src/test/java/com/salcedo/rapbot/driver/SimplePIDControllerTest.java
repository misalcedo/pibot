package com.salcedo.rapbot.driver;

import org.junit.Before;
import org.junit.Test;

import static java.lang.Math.*;

public class SimplePIDControllerTest {
    private static final double EPSILON = 0.0001;
    private PIDController pidController;

    @Before
    public void setUp() {
        pidController = new SimplePIDController(0.25, 0.01, 0.4);
    }

    @Test
    public void getOutput() {
        final double target = toRadians(15);
        PIDResult previousStep = new PIDResult(toRadians(270));

        System.err.print("Target\tActual\tOutput\tError\n");

        // Position based test code
        for (int i = 0; i < 100; i++){
            final double actual = previousStep.getActual() + previousStep.getOutput();
            final PIDResult step = pidController.step(adjustRadians(actual, target), adjustRadians(target, actual), previousStep);

            System.err.printf(
                    "%3.2f\t%3.2f\t%3.2f\t%3.2f\n",
                    toDegrees(adjustRadians(target, actual)),
                    toDegrees(step.getActual()),
                    toDegrees(step.getOutput()),
                    toDegrees(adjustRadians(target, actual) - adjustRadians(actual, target))
            );

            previousStep = step;
        }
    }

    private double adjustRadians(double x, double y) {
        return isCloseToPi(y - x) ? x + (2 * PI) : x;
    }

    private boolean isCloseToPi(double delta) {
        return delta >= PI + EPSILON || delta >= PI - EPSILON;
    }

}