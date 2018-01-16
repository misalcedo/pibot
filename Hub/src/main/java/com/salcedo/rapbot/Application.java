package com.salcedo.rapbot;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.model.Uri;
import com.salcedo.rapbot.hub.Hub;
import com.salcedo.rapbot.userinterface.GraphicalUserInterface;
import com.salcedo.rapbot.userinterface.GraphicalUserInterfaceFactory;
import kamon.Kamon;
import kamon.prometheus.PrometheusReporter;
import kamon.zipkin.ZipkinReporter;

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

        final Uri pi2 = Uri.create("http://192.168.1.42");
        final Uri zero = Uri.create("http://192.168.1.23");
        final Uri motorService = pi2.port(3000);
        final Uri visionService = pi2.port(3001);
        final Uri senseService = zero.port(3002);
        final Uri videoFeed = visionService.addPathSegment("/stream.mjpg");

        final GraphicalUserInterface ui = GraphicalUserInterfaceFactory.awt(system, videoFeed);
        final Props hubProps = Hub.props(motorService, visionService, senseService, ui);

        system.actorOf(hubProps, "hub");
        system.registerOnTermination(Kamon::stopAllReporters);
        system.registerOnTermination(Kamon::stopAllReporters);

        invokeLater(ui::display);
    }
}
