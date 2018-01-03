package com.salcedo.rapbot.driver;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MiniPIDTest {
    private MiniPID miniPID;

    @Before
    public void setUp() throws Exception {
        miniPID = new MiniPID(0.25, 0.01, 0.4);
        miniPID.setOutputLimits(10);
        //miniPID.setMaxIOutput(2);
        //miniPID.setOutputRampRate(3);
        //miniPID.setOutputFilter(.3);
        miniPID.setSetpointRange(40);
    }

    @Test
    public void getOutput() {
        double target = 100;

        double actual = 0;
        double output = 0;

        miniPID.setSetpoint(0);
        miniPID.setSetpoint(target);

        System.err.printf("Target\tActual\tOutput\tError\n");

        // Position based test code
        for (int i = 0; i < 100; i++){
            if (i == 60)
                target = 50;

            output = miniPID.step(actual, target).getOutput();
            actual = actual + output;

            System.err.printf("%3.2f\t%3.2f\t%3.2f\t%3.2f\n", target, actual - output, output, (target-actual));
        }
    }
}