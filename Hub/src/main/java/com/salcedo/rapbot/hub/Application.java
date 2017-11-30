package com.salcedo.rapbot.hub;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.model.Uri;
import com.salcedo.rapbot.userinterface.GraphicalUserInterface;
import com.salcedo.rapbot.userinterface.GraphicalUserInterfaceFactory;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;

import static javax.swing.SwingUtilities.invokeLater;

public final class Application {
    private final ActorSystem system;
    private final ActorRef hub;
    private final GraphicalUserInterface gui;

    private Application() {
        final Uri pi2 = Uri.create("http://192.168.1.42");
        final Uri zero = Uri.create("http://192.168.1.23");

        system = ActorSystem.create("RapBot");
        hub = system.actorOf(Hub.props(pi2, sqlContext()), "hub");
        //gui = GraphicalUserInterfaceFactory.video(system, pi2.port(3001).addPathSegment("/stream.mjpg"));
        gui = GraphicalUserInterfaceFactory.keyboard(system);
    }

    private SQLContext sqlContext() {
        final SparkContext sparkContext = new SparkContext("local[*]", "RapBot");
        final SparkSession sparkSession = new SparkSession(sparkContext);

        return new SQLContext(sparkSession);
    }

    public static void main(final String[] arguments) throws Exception {
        final Application application = new Application();

        invokeLater(application.gui::display);
    }
}
