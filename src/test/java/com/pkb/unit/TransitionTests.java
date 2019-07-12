package com.pkb.unit;

import static com.pkb.unit.Command.DISABLE;
import static com.pkb.unit.Command.START;
import static com.pkb.unit.TestCommon.assertTracker;
import static com.pkb.unit.message.ImmutableMessage.message;

import org.junit.After;
import org.junit.Test;

import com.pkb.unit.message.Message;
import com.pkb.unit.message.payload.Dependencies;
import com.pkb.unit.message.payload.Transition;

import io.reactivex.observers.TestObserver;

public class TransitionTests {

    private TestObserver<Message> testTransitionObserver;
    private TestObserver<Message> testDependencyObserver;

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

    // d3cdc4
    @Test
    public void disableCommandTransitionsSingleUnitToDisabled() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        testTransitionObserver = testTransitionObserver(bus);
        String unit1ID = "unit1";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        bus.sink().accept(command(unit1ID, START));
        unit1.completeStart();

        // WHEN
        bus.sink().accept(command(unit1ID, DISABLE));
        unit1.completeStop();

        // THEN
        testTransitionObserver
                .awaitCount(4);
        assertTracker(tracker);
    }

    // 78401e
    @Test
    public void disableCommandTransitionsMoreUnitsToDisabled() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        TestObserver<Message> testTransitionObserver = testTransitionObserver(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        bus.sink().accept(command(unit1ID, START));
        unit1.completeStart();
        bus.sink().accept(command(unit2ID, START));
        unit2.completeStart();

        // WHEN
        bus.sink().accept(command(unit1ID, DISABLE));
        unit1.completeStop();
        bus.sink().accept(command(unit2ID, DISABLE));
        unit2.completeStop();

        // THEN
        testTransitionObserver
                .awaitCount(8);
        assertTracker(tracker);
    }

    // 3c8925
    @Test
    public void whenSignleUnitAlreadyDisabledThenDisableDoesNothing() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        testTransitionObserver = testTransitionObserver(bus);
        String unit1ID = "unit1";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        bus.sink().accept(command(unit1ID, START));
        unit1.completeStart();
        bus.sink().accept(command(unit1ID, DISABLE));
        unit1.completeStop();
        testTransitionObserver
                .awaitCount(4);

        // WHEN
        bus.sink().accept(command(unit1ID, DISABLE));

        // THEN
        testTransitionObserver
                .awaitCount(5);
        assertTracker(tracker);
    }

    // FIXME: if unit2's START command arrives after that unit1 turns to DISABLED then unit1 turns to failed
    // GDE-1373 would make it reliably testable via manually start unit1 before adding unit2 as its dependency
    // 262d7c
    @Test
    public void dependencyStartDoesNotStartDisabledDependent() throws Exception {
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
        bus.sink().accept(command(unit1ID, START)); // starts unit2 dependency as well
        unit2.completeStart(); // unit2 reports CREATED here therefore unit1 receives START command from itself
        unit1.completeStart();
        bus.sink().accept(command(unit1ID, DISABLE));
        unit2.completeStop();
        unit1.completeStop();

        // THEN
        testTransitionObserver
                .awaitCount(6);
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

    // utility
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

    private Message<Command> command(String targetUnitId, Command command) {
        return message(Command.class)
                .withTarget(targetUnitId)
                .withPayload(command);
    }
}
