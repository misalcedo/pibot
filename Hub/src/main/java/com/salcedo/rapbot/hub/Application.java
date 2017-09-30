package com.salcedo.rapbot.hub;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.salcedo.rapbot.hub.driver.DriveRequest;
import com.salcedo.rapbot.userinterface.EventSource;
import com.salcedo.rapbot.userinterface.GraphicalUserInterface;

import static akka.actor.ActorRef.noSender;
import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.UIManager.setLookAndFeel;


public final class Application {
    private final ActorSystem system = ActorSystem.create("RapBot");
    private final ActorRef rapBot = system.actorOf(Props.create(RapBot.class));
    private final EventSource eventSource = new GraphicalUserInterface();

    public static void main(String[] arguments) throws Exception {
        setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");

        final Application application = new Application();
        application.rapBot.tell(new DriveRequest(), noSender());

        invokeLater(application::run);
    }

    private void run() {
        eventSource.listen(rapBot);
    }
}
