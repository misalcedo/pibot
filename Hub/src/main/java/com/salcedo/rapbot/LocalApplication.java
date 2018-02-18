package com.salcedo.rapbot;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.model.Uri;
import com.salcedo.rapbot.hub.Hub;
import com.salcedo.rapbot.hub.ServiceFactory;
import com.salcedo.rapbot.hub.SingletonServiceFactory;
import com.salcedo.rapbot.locomotion.MotorService;
import com.salcedo.rapbot.locomotion.MotorServiceFactory;
import com.salcedo.rapbot.sense.SenseService;
import com.salcedo.rapbot.sense.SenseServiceFactory;
import com.salcedo.rapbot.userinterface.GraphicalUserInterfaceFactory;
import com.salcedo.rapbot.userinterface.SwingGraphicalUserInterface;
import com.salcedo.rapbot.vision.VisionService;
import com.salcedo.rapbot.vision.VisionServiceFactory;
import kamon.Kamon;
import kamon.prometheus.PrometheusReporter;
import kamon.zipkin.ZipkinReporter;

import java.nio.file.Path;
import java.nio.file.Paths;

import static javax.swing.SwingUtilities.invokeLater;

public final class LocalApplication {
    private final ActorSystem system;


    private LocalApplication() {
        this.system = ActorSystem.create("RapBot");
    }

    public static void main(final String[] arguments) {
        new LocalApplication().run();
    }

    private void run() {
        Kamon.addReporter(new PrometheusReporter());
        Kamon.addReporter(new ZipkinReporter());

        final Uri videoFeed = Uri.create("https://www.youtube.com/watch?v=EZW7et3tPuQ");
        final Path workingDirectory = Paths.get("/home", "miguel", "IdeaProjects", "RapBot", "data", "test");

        final SwingGraphicalUserInterface ui = GraphicalUserInterfaceFactory.awt(system, videoFeed);

        final MotorService motorService = MotorServiceFactory.stub();
        final VisionService visionService = VisionServiceFactory.vlcj(ui.getMediaPlayer(), workingDirectory);
        final SenseService senseService = SenseServiceFactory.http(system, Uri.create(""));

        final ServiceFactory serviceFactory = new SingletonServiceFactory(motorService, visionService, senseService);
        final Props hubProps = Hub.props(serviceFactory, ui, workingDirectory);

        system.actorOf(hubProps, "hub");
        system.registerOnTermination(Kamon::stopAllReporters);

        ui.onClose(system::terminate);
        invokeLater(ui::display);
    }
}
