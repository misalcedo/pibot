package com.salcedo.rapbot;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.model.Uri;
import com.salcedo.rapbot.hub.Hub;
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
        final SwingGraphicalUserInterface ui = GraphicalUserInterfaceFactory.awt(system, videoFeed);
        final MotorService motorService = MotorServiceFactory.stub();
        final VisionService visionService = VisionServiceFactory.vlcj(ui.getMediaPlayer());
        final SenseService senseService = SenseServiceFactory.http(system, Uri.create(""));
        final Props hubProps = Hub.props(motorService, visionService, senseService, ui);

        system.actorOf(hubProps, "hub");
        system.registerOnTermination(Kamon::stopAllReporters);

        invokeLater(ui::display);
    }
}
