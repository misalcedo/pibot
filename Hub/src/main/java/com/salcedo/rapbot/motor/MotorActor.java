package com.salcedo.rapbot.motor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.salcedo.rapbot.snapshot.RegisterSubSystemMessage;
import com.salcedo.rapbot.snapshot.SnapshotMessage;
import com.salcedo.rapbot.snapshot.TakeSnapshotMessage;

public final class MotorActor extends AbstractActor {
    private static final Motor RELEASE_BACK_LEFT_MOTOR = Motor.builder()
            .withBackLeftLocation()
            .withReleaseCommand()
            .withSpeed(0)
            .build();
    private static final Motor RELEASE_BACK_RIGHT_MOTOR = Motor.builder()
            .withBackRightLocation()
            .withReleaseCommand()
            .withSpeed(0)
            .build();
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final MotorService motorService;
    private MotorResponse lastResponse;

    public MotorActor(final MotorService motorService) {
        this.motorService = motorService;
        this.lastResponse = MotorResponse.builder()
                .addMotor(RELEASE_BACK_LEFT_MOTOR)
                .addMotor(RELEASE_BACK_RIGHT_MOTOR)
                .build();
    }

    @Override
    public void preStart() throws Exception {
        release();
        context().system().eventStream().publish(new RegisterSubSystemMessage(self()));
        context().system().eventStream().subscribe(self(), TakeSnapshotMessage.class);
    }

    @Override
    public void postStop() throws Exception {
        release();
    }

    private void release() {
        final MotorRequest request = MotorRequest.builder()
                .addMotor(RELEASE_BACK_LEFT_MOTOR)
                .addMotor(RELEASE_BACK_RIGHT_MOTOR)
                .build();

        motorService.drive(request);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(MotorRequest.class, this::drive)
                .match(MotorResponse.class, this::respond)
                .match(TakeSnapshotMessage.class, message -> sender().tell(new SnapshotMessage(message.getUuid(), lastResponse), self()))
                .build();
    }

    private void respond(MotorResponse response) {
        lastResponse = response;

        sender().tell(response, self());
    }

    private void drive(final MotorRequest request) {
        log.info("Received a motor request. Request: {}", request);

        final ActorRef sender = sender();

        motorService.drive(request).thenAccept(response -> self().tell(response, sender));
    }
}
