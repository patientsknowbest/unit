package com.pkb.unit;

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

    // dc2a54
    @Test
    public void unsuccessfulStartTransitionsSingleUnitToFailing() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        String unitID = "unit1";
        FakeUnit unit1 = new FakeUnit(unitID, bus);
        testTransitionObserver = testTransitionObserver(bus);
        bus.sink().accept(command(unitID, Command.START));

        // WHEN
        unit1.failStart();

        // THEN
        testTransitionObserver
                .awaitCount(2);
        assertTracker(tracker);
    }

    // 366ae3
    @Test
    public void unsuccessfulStartTransitionsIndependentUnitsToFailing() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        testTransitionObserver = testTransitionObserver(bus);
        bus.sink().accept(command(unit1ID, Command.START));
        bus.sink().accept(command(unit2ID, Command.START));

        // WHEN
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
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        testTransitionObserver = testTransitionObserver(bus);
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);


        // WHEN
        unit1.addDependency(unit2ID);
        bus.sink().accept(command(unit1ID, Command.START));
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
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        testTransitionObserver = testTransitionObserver(bus);
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        bus.sink().accept(command(unit1ID, Command.START));


        // WHEN
        unit1.addDependency(unit2ID);
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
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        testTransitionObserver = testTransitionObserver(bus);
        Tracker tracker = new Tracker(bus);


        // WHEN
        bus.sink().accept(command(unit1ID, Command.START));
        unit1.addDependency(unit2ID);
        unit1.addDependency(unit3ID);
        unit2.failStart();
        unit3.completeStart();

        // THEN
        testTransitionObserver
                .awaitCount(10);
        assertTracker(tracker);
    }

    // b35bc4
    @Test
    public void unsuccessfulTransitiveDependencyStartTransitionsItToFailedAndDependentsToStarting() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        testTransitionObserver = testTransitionObserver(bus);
        testDependencyObserver = testDependencyObserver(bus);
        Tracker tracker = new Tracker(bus);


        // WHEN
        unit1.addDependency(unit2ID);
        unit2.addDependency(unit3ID);

        // THEN
        testDependencyObserver.awaitCount(2);

        // WHEN
        bus.sink().accept(command(unit1ID, Command.START));
        unit3.failStart();

        // THEN
        testTransitionObserver
                .awaitCount(9);
        assertTracker(tracker);
    }

    // 5d4caf
    @Test
    public void unsuccessfulStopTransitionsDependencyToFailedAndDependentToStopped() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        testTransitionObserver = testTransitionObserver(bus);
        testDependencyObserver = testDependencyObserver(bus);
        Tracker tracker = new Tracker(bus);

        // WHEN
        unit1.addDependency(unit2ID);

        // THEN
        testDependencyObserver
                .awaitCount(3);

        // WHEN
        bus.sink().accept(command(unit1ID, Command.START));
        unit1.completeStart();
        unit2.completeStart();

        // THEN
        testTransitionObserver
                .awaitCount(8); // make sure START command arrives earlier than STOP

        // WHEN
        bus.sink().accept(command(unit2ID, Command.STOP));
        unit2.failStop();
        unit1.completeStop();

        // THEN
        testTransitionObserver
                .awaitCount(12);
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
