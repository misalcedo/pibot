package com.salcedo.rapbot.driver;

import org.junit.Before;
import org.junit.Test;

public class SimplePIDControllerTest {
    private PIDController pidController;

    @Before
    public void setUp() {
        pidController = new MiniPID(0.25, 0.01, 0.4);
    }

    @Test
    public void getOutput() {
        final double target = 100;
        PIDResult previousStep = new PIDResult(0.0);

        System.err.print("Target\tActual\tOutput\tError\n");

        // Position based test code
        for (int i = 0; i < 200; i++){
            final double actual = previousStep.getActual() + previousStep.getOutput();
            final PIDResult step = pidController.step(actual, target);

            System.err.printf(
                    "%3.2f\t%3.2f\t%3.2f\t%3.2f\n",
                    target,
                    step.getActual(),
                    step.getOutput(),
                    target - actual
            );

            previousStep = step;
        }
    }
}