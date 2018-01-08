package com.salcedo.rapbot.driver;

public interface Range {
    int first();
    int last();
    int distance();
    int modulo(int value);
    int bounded(int value);
}
