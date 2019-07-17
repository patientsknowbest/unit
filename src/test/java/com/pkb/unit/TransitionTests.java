package com.pkb.unit;

import static com.pkb.unit.Command.ENABLE;
import static com.pkb.unit.Command.START;
import static com.pkb.unit.Command.STOP;
import static com.pkb.unit.DesiredState.ENABLED;
import static com.pkb.unit.DesiredState.UNSET;
import static com.pkb.unit.State.STARTED;
import static com.pkb.unit.State.STARTING;
import static com.pkb.unit.message.ImmutableMessage.message;
import static com.pkb.unit.tracker.ImmutableSystemState.systemState;
import static com.pkb.unit.tracker.ImmutableUnit.unit;

import org.junit.After;
import org.junit.Test;

import com.pkb.unit.message.Message;
import com.pkb.unit.message.payload.Transition;
import com.pkb.unit.tracker.SystemState;

import com.google.common.collect.ImmutableMap;

import io.reactivex.observers.TestObserver;

public class TransitionTests extends AbstractUnitTest {

    private TestObserver<Message> testTransitionObserver;

    @Test
    public void enableCommandTransitionsSingleUnitToStarting() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        SystemState expected = systemState(
                ImmutableMap.of("unit1", unit("unit1").withState(STARTING).withDesiredState(ENABLED))
        );
        TestObserver<SystemState> testObserver = testObserver();

        // WHEN
        new FakeUnit("unit1", bus);
        bus.sink().accept(command("unit1", ENABLE));

        // THEN
        testComputationScheduler.triggerActions();
        assertExpectedState(testObserver, expected);
    }

    @Test
    public void enableCommandTransitionsUnitAndDependencyToStarting() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        SystemState expected = systemState(
                ImmutableMap.of(
                        "unit1", unit("unit1").withState(STARTING).withDesiredState(ENABLED).withDependencies("unit2"),
                        "unit2", unit("unit2").withState(STARTING).withDesiredState(UNSET)
                )
        );
        TestObserver<SystemState> testObserver = testObserver();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        FakeUnit unit2 = new FakeUnit("unit2", bus);
        unit1.addDependency("unit2");
        bus.sink().accept(command("unit1", ENABLE));

        testComputationScheduler.triggerActions();
        assertExpectedState(testObserver, expected);
    }

    @Test
    public void completeStartTransitionsUnitsToStarted() throws Exception {
        // GIVEN
        setupTestScheduler();
        SystemState expected = systemState(
                ImmutableMap.of(
                        "unit1", unit("unit1").withState(STARTED).withDesiredState(ENABLED).withDependencies("unit2"),
                        "unit2", unit("unit2").withState(STARTED).withDesiredState(UNSET)
                )
        );
        TestObserver<SystemState> testObserver = testObserver();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        FakeUnit unit2 = new FakeUnit("unit2", bus);
        unit1.addDependency("unit2");
        bus.sink().accept(command("unit1", ENABLE));
        unit2.completeStart();
        unit1.completeStart();

        testScheduler.triggerActions();
        assertExpectedState(testObserver, expected);
    }

    // ccdaf8
    @Test
    public void whenSingleUnitAlreadyStartedThenStartDoesNothing() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        //Tracker tracker = new Tracker(bus);
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
        //assertTracker(tracker);
    }

    // 5c4777
    @Test
    public void whenDependencyAlreadyStartedThenStartDoesNothing() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        //Tracker tracker = new Tracker(bus);
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
        //assertTracker(tracker);
    }

    // ceb5f8
    @Test
    public void whenSingleUnitAlreadyStoppedThenStopDoesNothing() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        //Tracker tracker = new Tracker(bus);
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
        //assertTracker(tracker);
    }

    // fe5fbb
    @Test
    public void whenDependencyAlreadyStoppedThenStopDoesNothing() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        //Tracker tracker = new Tracker(bus);
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
        //assertTracker(tracker);
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
