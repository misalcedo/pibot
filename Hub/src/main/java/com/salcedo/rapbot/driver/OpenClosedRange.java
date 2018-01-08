package com.salcedo.rapbot.driver;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class OpenClosedRange implements Range {
    private final int start;
    private final int end;

    public OpenClosedRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public int first() {
        return start;
    }

    @Override
    public int last() {
        return end;
    }

    @Override
    public int modulo(final int value) {
        int remainder = value % distance();
        int negativeAdjustment = remainder + distance();

        return first() + (negativeAdjustment % distance());
    }

    @Override
    public int bounded(int value) {
        int endBounded = min(value, last());
        return max(endBounded, first());
    }

    @Override
    public int distance() {
        return last() - first();
    }
}
