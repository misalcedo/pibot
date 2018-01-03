package com.salcedo.rapbot.driver;

public interface Range {
    int first();
    int last();
    int modulo(int value);
    int bounded(int value);
    boolean isStartInclusive();
    boolean isEndInclusive();
}
