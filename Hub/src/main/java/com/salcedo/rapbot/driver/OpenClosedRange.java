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
    public int start() {
        return isStartInclusive() ? start : start - 1;
    }

    @Override
    public int end() {
        return isEndInclusive() ? end : end - 1;
    }

    @Override
    public int modulo(final int value) {
        // TODO: fix modulo operator to actually work as a modulo.
        return start + (value % getDistance());
    }

    @Override
    public int bounded(int value) {
        int endBounded = min(value, end());
        return max(endBounded, start());
    }

    private int getDistance() {
        return end - start;
    }

    @Override
    public boolean isStartInclusive() {
        return startInclusive;
    }

    @Override
    public boolean isEndInclusive() {
        return endInclusive;
    }

    @Override
    public boolean isInRange(int value) {
        final boolean greaterThanStart = isStartInclusive() ? value >= start : value > start;
        final boolean lessThanEnd = isEndInclusive() ? value <= end : value < end;
        return greaterThanStart && lessThanEnd;
    }
}
