package com.salcedo.rapbot.sense;

public class ThreeDimensionalSensorReading {
    private final double x;
    private final double y;
    private final double z;

    public ThreeDimensionalSensorReading(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    @Override
    public String toString() {
        return "ThreeDimensionalSensorReading{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
