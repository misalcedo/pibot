package com.salcedo.rapbot.locomotion;

import com.google.gson.annotations.SerializedName;

import java.util.Optional;

/**
 * The location of the locomotion relative to the bow of the robot.
 */
public enum Location {
    @SerializedName("0") BACK_LEFT, @SerializedName("1") BACK_RIGHT;

    public Optional<Motor> getMotor(final MotorResponse response) {
        return response.getMotor(this);
    }
}
