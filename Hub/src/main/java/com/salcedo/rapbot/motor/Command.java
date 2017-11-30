package com.salcedo.rapbot.motor;

import com.google.gson.annotations.SerializedName;

/**
 * The command denotes the direction the engine will rotate.
 */
public enum Command {
    @SerializedName("0") FORWARD, @SerializedName("1") BACKWARD, @SerializedName("2") BRAKE, @SerializedName("3") RELEASE
}
