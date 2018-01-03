package com.salcedo.rapbot.driver;

class PIDResult {
    private final double actual;
    private final double output;
    private final double cumulativeError;

    PIDResult(double actual, double output, double cumulativeError) {
        this.actual = actual;
        this.output = output;
        this.cumulativeError = cumulativeError;
    }

    PIDResult(double actual) {
        this(actual, 0.0, 0.0);
    }

    public double getActual() {
        return actual;
    }

    public double getOutput() {
        return output;
    }

    public double getCumulativeError() {
        return cumulativeError;
    }
}