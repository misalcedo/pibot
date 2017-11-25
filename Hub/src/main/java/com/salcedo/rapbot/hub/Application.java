package com.salcedo.rapbot.hub;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.model.Uri;
import com.salcedo.rapbot.sense.OrientationRequest;
import com.salcedo.rapbot.userinterface.GraphicalUserInterface;
import com.salcedo.rapbot.userinterface.GraphicalUserInterfaceFactory;

import java.net.URI;

import static akka.actor.ActorRef.noSender;
import static javax.swing.SwingUtilities.invokeLater;

public final class Application {
    private final ActorSystem system;
    private final ActorRef rapBot;
    private final GraphicalUserInterface gui;

    private Application() {
        final Uri pi2 = Uri.create("http://192.168.1.42");
        final Uri zero = Uri.create("http://192.168.1.23");

        system = ActorSystem.create("RapBot");
        rapBot = system.actorOf(Props.create(RapBot.class, pi2));
        //gui = GraphicalUserInterfaceFactory.video(system, pi2.port(3001).addPathSegment("/stream.mjpg"));
        gui = GraphicalUserInterfaceFactory.keyboard(system);
    }

    public static void main(final String[] arguments) throws Exception {
        final Application application = new Application();

        invokeLater(application.gui::display);
    }
}
