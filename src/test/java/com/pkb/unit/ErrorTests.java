package com.pkb.unit;

import static com.pkb.unit.TestCommon.assertTracker;
import static com.pkb.unit.message.ImmutableMessage.message;

import org.junit.Test;

import com.pkb.unit.message.Message;
import com.pkb.unit.message.payload.Transition;

import io.reactivex.observers.TestObserver;

// 729d5a
public class ErrorTests {

    // dc2a54
    @Test
    public void unsuccessfulStartTransitionsSingleUnitToFailing() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        String unitID = "unit1";
        FakeUnit unit1 = new FakeUnit(unitID, bus);
        TestObserver<Message> testTransitionObserver = testTransitionObserver(bus);
        bus.sink().accept(command(unitID, Command.START));

        // WHEN
        unit1.failStart();

        // THEN
        testTransitionObserver
                .awaitCount(2);
        assertTracker(tracker);
    }

    private TestObserver<Message> testTransitionObserver(Bus bus) {
        return bus.events()
                .filter(it -> it.messageType().equals(Transition.class))
                .test();
    }

    private Message<Command> command(String targetUnitId, Command start) {
        return message(Command.class)
                .withTarget(targetUnitId)
                .withPayload(start);
    }
}
