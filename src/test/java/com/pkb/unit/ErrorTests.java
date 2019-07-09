package com.pkb.unit;

import static com.pkb.unit.Command.START;
import static com.pkb.unit.Command.STOP;
import static com.pkb.unit.TestCommon.assertTracker;
import static com.pkb.unit.message.ImmutableMessage.message;

import org.junit.After;
import org.junit.Test;

import com.pkb.unit.message.Message;
import com.pkb.unit.message.payload.Dependencies;
import com.pkb.unit.message.payload.Transition;

import io.reactivex.observers.TestObserver;

// 729d5a
public class ErrorTests {

    TestObserver<Message> testTransitionObserver;
    TestObserver<Message> testDependencyObserver;

    // 0362b3
    @Test
    public void unsuccessfulStartTransitionsSingleUnitToFailed() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        testTransitionObserver = testTransitionObserver(bus);
        String unitID = "unit1";
        FakeUnit unit1 = new FakeUnit(unitID, bus);

        // WHEN
        bus.sink().accept(command(unitID, START));
        unit1.failStart();

        // THEN
        testTransitionObserver
                .awaitCount(2);
        assertTracker(tracker);
    }

    // 588954
    @Test
    public void unsuccessfulStartTransitionsIndependentUnitsToFailed() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        testTransitionObserver = testTransitionObserver(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);

        // WHEN
        bus.sink().accept(command(unit1ID, START));
        bus.sink().accept(command(unit2ID, START));
        unit1.failStart();
        unit2.failStart();

        // THEN
        testTransitionObserver
                .awaitCount(4);
        assertTracker(tracker);
    }

    // f859a1
    @Test
    public void unsuccessfulDependentStartTransitionsItToFailedAndDependencyToStarted() throws Exception {
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
        unit1.failStart();
        unit2.completeStart();

        // THEN
        testTransitionObserver
                .awaitCount(6);
        assertTracker(tracker);
    }

    // 3eb772
    @Test
    public void unsuccessfulDependencyStartTransitionsDependencyToFailedAndDependentToStarting() throws Exception {
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
        unit2.failStart();
        unit1.completeStart();

        // THEN
        testTransitionObserver
                .awaitCount(4);
        assertTracker(tracker);
    }

    // ca9c14
    @Test
    public void unsuccessfulDependencyStartTransitionsItToFailedAndDependentToStartingAndOtherDependencySarted() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        testTransitionObserver = testTransitionObserver(bus);
        testDependencyObserver = testDependencyObserver(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        unit1.addDependency(unit2ID);
        unit1.addDependency(unit3ID);

        // WHEN
        bus.sink().accept(command(unit1ID, START));
        unit2.failStart();
        unit3.completeStart();

        // THEN
        testTransitionObserver
                .awaitCount(7);
        assertTracker(tracker);
    }

    // b35bc4
    @Test
    public void unsuccessfulTransitiveDependencyStartTransitionsItToFailedAndDependentsToStarting() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        testTransitionObserver = testTransitionObserver(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        unit1.addDependency(unit2ID);
        unit2.addDependency(unit3ID);

        // WHEN
        bus.sink().accept(command(unit1ID, START));
        unit3.failStart();

        // THEN
        testTransitionObserver
                .awaitCount(6);
        assertTracker(tracker);
    }

    // cd5c29
    @Test
    public void unsuccessfulStopTransitionsSingleUnitToFailed() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        testTransitionObserver = testTransitionObserver(bus);
        String unitID = "unit1";
        FakeUnit unit1 = new FakeUnit(unitID, bus);

        // WHEN
        bus.sink().accept(command(unitID, START));
        unit1.completeStart();
        bus.sink().accept(command(unitID, STOP));
        unit1.failStop();

        // THEN
        testTransitionObserver
                .awaitCount(4);
        assertTracker(tracker);
    }

    // 4783e6
    @Test
    public void unsuccessfulStopTransitionsIndependentUnitsToFailed() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        testTransitionObserver = testTransitionObserver(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        bus.sink().accept(command(unit1ID, START));
        bus.sink().accept(command(unit2ID, START));
        unit1.completeStart();
        unit2.completeStart();

        // WHEN
        bus.sink().accept(command(unit1ID, STOP));
        bus.sink().accept(command(unit2ID, STOP));
        unit1.failStop();
        unit2.failStop();

        // THEN
        testTransitionObserver
                .awaitCount(8);
        assertTracker(tracker);
    }

    // 5d4caf
    @Test
    public void unsuccessfulStopTransitionsDependencyToFailedAndDependentToStopped() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        testTransitionObserver = testTransitionObserver(bus);
        testDependencyObserver = testDependencyObserver(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        unit1.addDependency(unit2ID);

        // WHEN
        bus.sink().accept(command(unit1ID, START));
        unit1.completeStart();
        unit2.completeStart();

        // THEN
        testTransitionObserver
                .awaitCount(6); // make sure that START command arrives earlier than STOP

        // WHEN
        bus.sink().accept(command(unit2ID, STOP));
        unit2.failStop();
        unit1.completeStop();

        // THEN
        testTransitionObserver
                .awaitCount(10);
        assertTracker(tracker);
    }

    // 87402f
    @Test
    public void unsuccessfulTransitiveDependencyStopTransitionsItToFailedAndDependentsToStopped() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        testTransitionObserver = testTransitionObserver(bus);
        testDependencyObserver = testDependencyObserver(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        unit1.addDependency(unit2ID);
        unit2.addDependency(unit3ID);

        // WHEN
        bus.sink().accept(command(unit1ID, START));
        unit3.completeStart();
        unit2.completeStart();
        unit1.completeStart();

        // THEN
        testTransitionObserver
                .awaitCount(10); // make sure that START command arrives earlier than STOP

        // WHEN
        bus.sink().accept(command(unit3ID, STOP));
        unit3.failStop();
        unit2.completeStop();
        unit1.completeStop();

        // THEN
        testTransitionObserver
                .awaitCount(16);
        assertTracker(tracker);
    }

    @After
    public void disposeTestObservers() {
        if (testTransitionObserver != null && !testTransitionObserver.isDisposed()) {
            testTransitionObserver.dispose();
        }
        if (testDependencyObserver != null && !testDependencyObserver.isDisposed()) {
            testDependencyObserver.dispose();
        }
    }

    private TestObserver<Message> testTransitionObserver(Bus bus) {
        return bus.events()
                .filter(it -> it.messageType().equals(Transition.class))
                .test();
    }

    private TestObserver<Message> testDependencyObserver(Bus bus) {
        return bus.events()
                .filter(it -> it.messageType().equals(Dependencies.class))
                .test();
    }

    private Message<Command> command(String targetUnitId, Command start) {
        return message(Command.class)
                .withTarget(targetUnitId)
                .withPayload(start);
    }
}
