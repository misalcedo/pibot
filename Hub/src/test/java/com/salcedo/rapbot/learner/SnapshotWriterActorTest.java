package com.salcedo.rapbot.learner;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.TestActors;
import akka.testkit.TestProbe;
import com.salcedo.rapbot.snapshot.ObjectSnapshotMessage;
import com.salcedo.rapbot.snapshot.SingleResponseSystemSnapshot;
import com.salcedo.rapbot.snapshot.SystemSnapshot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static akka.testkit.JavaTestKit.shutdownActorSystem;
import static java.nio.file.Files.createTempDirectory;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;

public class SnapshotWriterActorTest {
    private ActorSystem system;
    private ActorRef writer;

    @Before
    public void setUp() throws Exception {
        system = ActorSystem.create();
        writer = system.actorOf(SnapshotWriterActor.props(createTempDirectory("writer")));
    }

    @After
    public void tearDown() {
        shutdownActorSystem(system);
    }

    @Test
    public void createReceive() {
        final TestProbe probe = new TestProbe(system);
        final ActorRef subsystem = system.actorOf(TestActors.echoActorProps());
        final SystemSnapshot systemSnapshot = new SingleResponseSystemSnapshot(randomUUID(), singleton(subsystem.path()));

        systemSnapshot.addSnapshot(new ObjectSnapshotMessage(systemSnapshot.getUuid(), new Object()), subsystem.path());

        probe.send(writer, systemSnapshot);
    }
}