package com.salcedo.rapbot.driver;

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
        return start;
    }

    @Override
    public int end() {
        return end;
    }

    @Override
    public int modulo(final int value) {
        return start + (value % getDistance());
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
