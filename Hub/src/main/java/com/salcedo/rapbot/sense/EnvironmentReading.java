package com.salcedo.rapbot.sense;

public class EnvironmentReading {
    /**
     * Current pressure in millibars.
     */
    private final double pressure;

    /**
     * The percentage of relative humidity.
     */
    private final double humidity;

    /**
     * Current temperature in degrees Celsius.
     */
    private final double temperature;

    /**
     * Current orientation in degrees using the aircraft principal axes of pitch, roll and yaw.
     */
    private final Orientation orientation;


    /**
     * Current orientation in degrees using the aircraft principal axes of pitch, roll and yaw.
     * Adjusted relative to the first orientation reading when the service started.
     */
    private final Orientation relative_orientation;


    /**
     * The direction of North from the magnetometer in degrees
     */
    private final double compass;

    /**
     * The raw x, y and z axis magnetometer data
     */
    private final ThreeDimensionalSensorReading magnetometer;

    /**
     * The raw x, y and z axis gyroscope data
     */
    private final ThreeDimensionalSensorReading gyroscope;

    /**
     * The raw x, y and z axis accelerometer data
     */
    private final ThreeDimensionalSensorReading accelerometer;

    public EnvironmentReading(
            double pressure,
            double humidity,
            double temperature,
            double compass,
            ThreeDimensionalSensorReading magnetometer,
            ThreeDimensionalSensorReading gyroscope,
            ThreeDimensionalSensorReading accelerometer,
            Orientation orientation,
            Orientation relativeOrientation) {
        this.pressure = pressure;
        this.humidity = humidity;
        this.temperature = temperature;
        this.orientation = orientation;
        this.compass = compass;
        this.magnetometer = magnetometer;
        this.gyroscope = gyroscope;
        this.accelerometer = accelerometer;
        this.relative_orientation = relativeOrientation;
    }

    public double getPressure() {
        return pressure;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getTemperature() {
        return temperature;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public Orientation getRelativeOrientation() {
        return relative_orientation;
    }

    public double getCompass() {
        return compass;
    }

    public ThreeDimensionalSensorReading getMagnetometer() {
        return magnetometer;
    }

    public ThreeDimensionalSensorReading getGyroscope() {
        return gyroscope;
    }

    public ThreeDimensionalSensorReading getAccelerometer() {
        return accelerometer;
    }

    @Override
    public String toString() {
        return "EnvironmentReading{" +
                "pressure=" + pressure +
                ", humidity=" + humidity +
                ", temperature=" + temperature +
                ", orientation=" + orientation +
                ", compass=" + compass +
                ", magnetometer=" + magnetometer +
                ", gyroscope=" + gyroscope +
                ", accelerometer=" + accelerometer +
                '}';
    }
}
