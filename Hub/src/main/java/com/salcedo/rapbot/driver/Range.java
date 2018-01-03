package com.salcedo.rapbot.driver;

public interface Range {
    int start();
    int end();
    int modulo(int value);
    int bounded(int value);
    boolean isStartInclusive();
    boolean isEndInclusive();
    boolean isInRange(int value);
}
