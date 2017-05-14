package com.salcedo.rapbot.hub;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public final class Main {
    public static void main(String[] arguments) throws Exception {
        final URL motorService = new URL("http://motor-service/");
        final URLConnection urlConnection = motorService.openConnection();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        reader.lines().forEach(System.out::println);
        reader.close();
    }
}
