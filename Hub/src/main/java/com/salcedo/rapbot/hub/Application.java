package com.salcedo.rapbot.hub;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.salcedo.rapbot.userinterface.GraphicalUserInterface;
import com.salcedo.rapbot.userinterface.GraphicalUserInterfaceFactory;

import static javax.swing.SwingUtilities.invokeLater;

public final class Application {
    private final ActorSystem system;
    private final ActorRef rapBot;
    private final GraphicalUserInterface gui;

    private Application() {
        system = ActorSystem.create("RapBot");
        rapBot = system.actorOf(Props.create(RapBot.class));
        gui = GraphicalUserInterfaceFactory.video(system);
    }

    public static void main(final String[] arguments) throws Exception {
        final Application application = new Application();

        invokeLater(application.gui::display);
    }
}
