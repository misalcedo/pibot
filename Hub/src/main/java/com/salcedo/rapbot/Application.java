package com.salcedo.rapbot;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.model.Uri;
import com.salcedo.rapbot.hub.Hub;
import com.salcedo.rapbot.locomotion.MotorService;
import com.salcedo.rapbot.locomotion.MotorServiceFactory;
import com.salcedo.rapbot.sense.SenseService;
import com.salcedo.rapbot.sense.SenseServiceFactory;
import com.salcedo.rapbot.userinterface.GraphicalUserInterface;
import com.salcedo.rapbot.userinterface.GraphicalUserInterfaceFactory;
import com.salcedo.rapbot.vision.VisionService;
import com.salcedo.rapbot.vision.VisionServiceFactory;
import kamon.Kamon;
import kamon.prometheus.PrometheusReporter;
import kamon.zipkin.ZipkinReporter;

import java.nio.file.Path;
import java.nio.file.Paths;

import static javax.swing.SwingUtilities.invokeLater;

public final class Application {
    private final ActorSystem system;

    private Application() {
        this.system = ActorSystem.create("RapBot");
    }

    public static void main(final String[] arguments) {
        new Application().run();
    }

    private void run() {
        Kamon.addReporter(new PrometheusReporter());
        Kamon.addReporter(new ZipkinReporter());

        final Uri pi2 = Uri.create("http://192.168.1.41");
        final Uri videoFeed = pi2.port(3001).addPathSegment("/stream.mjpg");
        final Path workingDirectory = Paths.get("/home", "miguel", "IdeaProjects", "RapBot", "data", "production");

        final GraphicalUserInterface ui = GraphicalUserInterfaceFactory.awt(system, videoFeed);

        final MotorService motorService = MotorServiceFactory.http(system, pi2.port(3000));
        final VisionService visionService = VisionServiceFactory.http(system, pi2.port(3001), workingDirectory);
        final SenseService senseService = SenseServiceFactory.http(system, pi2.port(3002));

        final Props hubProps = Hub.props(motorService, visionService, senseService, ui, workingDirectory);

        system.actorOf(hubProps, "hub");
        system.registerOnTermination(Kamon::stopAllReporters);

        invokeLater(ui::display);
    }
}
