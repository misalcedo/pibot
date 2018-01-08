package com.salcedo.rapbot.locomotion;

import com.google.gson.annotations.SerializedName;

/**
 * The command denotes the direction the engine will rotate.
 */
public enum Command {
    @SerializedName("1") FORWARD, @SerializedName("2") BACKWARD, @SerializedName("3") BRAKE, @SerializedName("4") RELEASE
}
