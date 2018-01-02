package com.salcedo.rapbot.locomotion;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.Uri;
import akka.stream.ActorMaterializer;
import com.google.gson.GsonBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class HttpMotorServiceTest {
    private MotorService service;
    private ActorSystem system;

    @Before
    public void setUp() {
        system = ActorSystem.create("Test");
        service = new HttpMotorService(
                Http.get(system),
                ActorMaterializer.create(system),
                Uri.create("http://192.168.1.41:3000/motors"),
                new GsonBuilder().create()
        );
    }

    @After
    public void tearDown() {
        system.terminate();
    }

    @Test
    @Ignore
    public void drive() throws Exception {
        service.drive(createRequest(255)).toCompletableFuture().get(1L, TimeUnit.SECONDS);
        service.drive(createRequest(0)).toCompletableFuture().get(1L, TimeUnit.SECONDS);
    }

    private MotorRequest createRequest(int speed) {
        Motor backLeftMotor = Motor.builder()
                .withBackLeftLocation()
                .withForwardCommand()
                .withSpeed(speed)
                .build();
        Motor backRightMotor = Motor.builder()
                .withBackRightLocation()
                .withForwardCommand()
                .withSpeed(speed)
                .build();

        return MotorRequest.builder()
                .addMotor(backLeftMotor)
                .addMotor(backRightMotor)
                .build();
    }
}