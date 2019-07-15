package com.pkb.unit;

import static com.pkb.unit.Command.START;
import static com.pkb.unit.Command.STOP;
import static com.pkb.unit.TestCommon.assertTracker;
import static com.pkb.unit.message.ImmutableMessage.message;

import org.junit.After;
import org.junit.Test;

import com.pkb.unit.message.Message;
import com.pkb.unit.message.payload.Transition;

import io.reactivex.observers.TestObserver;

public class TransitionTests {

    private TestObserver<Message> testTransitionObserver;

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
        bus.sink().accept(command(unitID, START));

        // THEN
        testTransitionObserver
                .awaitCount(1);
        assertTracker(tracker);
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

        bus.sink().accept(command(unit1ID, START));

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

        bus.sink().accept(command(unit1ID, START));
        unit2.completeStart();
        unit1.completeStart();

        testTransitionObserver
                .awaitCount(6);

        assertTracker(tracker);
    }

    // ccdaf8
    @Test
    public void whenSingleUnitAlreadyStartedThenStartDoesNothing() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        testTransitionObserver = testTransitionObserver(bus);
        String unitID = "unit1";
        FakeUnit unit1 = new FakeUnit(unitID, bus);
        bus.sink().accept(command(unitID, START));
        unit1.completeStart();

        // WHEN
        bus.sink().accept(command(unitID, START));

        // THEN
        testTransitionObserver
                .awaitCount(3);
        assertTracker(tracker);
    }

    // 5c4777
    @Test
    public void whenDependencyAlreadyStartedThenStartDoesNothing() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        testTransitionObserver = testTransitionObserver(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        unit1.addDependency(unit2ID);
        bus.sink().accept(command(unit1ID, START));
        unit2.completeStart();
        unit1.completeStart();

        // WHEN
        bus.sink().accept(command(unit2ID, START));

        // THEN
        testTransitionObserver
                .awaitCount(6);
        assertTracker(tracker);
    }

    // ceb5f8
    @Test
    public void whenSingleUnitAlreadyStoppedThenStopDoesNothing() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        testTransitionObserver = testTransitionObserver(bus);
        String unitID = "unit1";
        FakeUnit unit1 = new FakeUnit(unitID, bus);
        bus.sink().accept(command(unitID, START));
        unit1.completeStart();
        bus.sink().accept(command(unitID, STOP));
        unit1.completeStop();

        // WHEN
        bus.sink().accept(command(unitID, STOP));

        // THEN
        testTransitionObserver
                .awaitCount(5);
        assertTracker(tracker);
    }

    // fe5fbb
    @Test
    public void whenDependencyAlreadyStoppedThenStopDoesNothing() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        testTransitionObserver = testTransitionObserver(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        unit1.addDependency(unit2ID);

        // WHEN
        bus.sink().accept(command(unit1ID, START));
        unit2.completeStart();
        unit1.completeStart();

        // THEN
        testTransitionObserver
                .awaitCount(6); // Make sure that START command arrives earlier than STOP

        // WHEN
        bus.sink().accept(command(unit2ID, STOP));
        unit2.completeStop();
        unit1.completeStop();
        bus.sink().accept(command(unit2ID, STOP));

        // THEN
        testTransitionObserver
                .awaitCount(11);
        assertTracker(tracker);
    }

    @After
    public void disposeTestObservers() {
        if (testTransitionObserver != null && !testTransitionObserver.isDisposed()) {
            testTransitionObserver.dispose();
        }
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
