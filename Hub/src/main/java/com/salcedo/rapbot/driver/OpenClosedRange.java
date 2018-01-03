package com.salcedo.rapbot.driver;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class OpenClosedRange implements Range {
    private final boolean startInclusive;
    private final boolean endInclusive;
    private final int start;
    private final int end;

    public OpenClosedRange(boolean startInclusive, boolean endInclusive, int start, int end) {
        this.startInclusive = startInclusive;
        this.endInclusive = endInclusive;
        this.start = start;
        this.end = end;
    }

    @Override
    public int first() {
        return isStartInclusive() ? start : start - 1;
    }

    @Override
    public int last() {
        return isEndInclusive() ? end : end - 1;
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

    private int distance() {
        return last() - first();
    }

    @Override
    public boolean isStartInclusive() {
        return startInclusive;
    }

    @Override
    public boolean isEndInclusive() {
        return endInclusive;
    }
}
