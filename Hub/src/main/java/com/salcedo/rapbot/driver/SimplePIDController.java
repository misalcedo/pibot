package com.salcedo.rapbot.driver;

public class SimplePIDController implements PIDController {
    private final double proportionalGain;
    private final double integralGain;
    private final double derivativeGain;
    private final double feedForwardGain;

    /**
     * Create a MiniPID class object.
     * See setProportionalGain, setI, setD, setF methods for more detailed parameters.
     * @param proportionalGain Proportional gain. Large if large difference between setPoint and target.
     * @param integralGain Integral gain.  Becomes large if setPoint cannot reach target quickly.
     * @param derivativeGain Derivative gain. Responds quickly to large changes in error. Small values prevents proportionalGain and I terms from causing overshoot.
     * @param feedForwardGain Feed-forward gain. Open loop "best guess" for the output should be. Only useful if setPoint represents a rate.
     */
    public SimplePIDController(
            final double proportionalGain,
            final double integralGain,
            final double derivativeGain,
            final double feedForwardGain
    ){
        if (proportionalGain < 0 || integralGain < 0 || derivativeGain < 0 || feedForwardGain < 0) {
            throw new IllegalArgumentException(
                    String.format(
                            "Negative gain is not supported. Proportional: %f, Integral: %f, Derivative: %f, Feed-Forward: %f",
                            proportionalGain,
                            integralGain,
                            derivativeGain,
                            feedForwardGain
                    )
            );
        }

        this.proportionalGain = proportionalGain;
        this.integralGain = integralGain;
        this.derivativeGain = derivativeGain;
        this.feedForwardGain = feedForwardGain;
    }

    @Override
    public PIDResult step(double actual, double target, PIDResult previousStep) {
        final double error = target - actual;

        final double feedForwardOutput = feedForwardGain * target;
        final double proportionalOutput = proportionalGain * error;
        final double derivativeOutput = -derivativeGain * (actual - previousStep.getActual());
        final double integralOutput = integralGain * previousStep.getCumulativeError();

        final double output = feedForwardOutput + proportionalOutput + integralOutput + derivativeOutput;

        return new PIDResult(actual, output, error + previousStep.getCumulativeError());
    }
}
