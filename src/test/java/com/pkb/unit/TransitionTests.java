package com.pkb.unit;

import static com.pkb.unit.TestCommon.assertTracker;
import static com.pkb.unit.message.ImmutableMessage.message;

import org.junit.Test;

import com.pkb.unit.message.Message;
import com.pkb.unit.message.payload.Transition;

import io.reactivex.observers.TestObserver;

public class TransitionTests {

    /**
     * START command should trigger the
     * ffc147
     */
    @Test
    public void startCommandTransitionsSingleUnitToStarting() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        String unitID = "unit1";
        new FakeUnit(unitID, bus);
        TestObserver<Message> testTransitionObserver = testTransitionObserver(bus);

        // WHEN
        bus.sink().accept(command(unitID, Command.START));

        // THEN
        testTransitionObserver
                .awaitCount(1);
        assertTracker(tracker);
    }

    private TestObserver<Message> testTransitionObserver(Bus bus) {
        return bus.events()
                .filter(it -> it.messageType().equals(Transition.class))
                .test();
    }

    // 9070ca
    @Test
    public void startCommandTransitionsUnitToStarting() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        TestObserver<Message> testTransitionObserver = testTransitionObserver(bus);

        // WHEN
        unit1.addDependency(unit2ID);

        bus.sink().accept(command(unit1ID, Command.START));

        testTransitionObserver
                .awaitCount(3); // Should see 2 transitions

        assertTracker(tracker);
    }

    // 7c64a1
    @Test
    public void startCompleteTransitionsUnitsToStarted() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        TestObserver<Message> testTransitionObserver = testTransitionObserver(bus);

        // WHEN
        unit1.addDependency(unit2ID);

        bus.sink().accept(command(unit1ID, Command.START));
        unit2.completeStart();
        unit1.completeStart();

        testTransitionObserver
                .awaitCount(6);

        assertTracker(tracker);
    }

    private Message<Command> command(String targetUnitId, Command start) {
        return message(Command.class)
                .withTarget(targetUnitId)
                .withPayload(start);
    }
}
