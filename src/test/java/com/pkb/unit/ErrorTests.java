package com.pkb.unit;

import static com.pkb.unit.Command.ENABLE;
import static com.pkb.unit.Command.START;
import static com.pkb.unit.Command.STOP;
import static com.pkb.unit.TestCommon.assertTracker;
import static com.pkb.unit.message.ImmutableMessage.message;

import java.util.Optional;

import org.junit.Test;

import com.pkb.unit.message.Message;
import com.pkb.unit.message.payload.Transition;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;

// 729d5a
public class ErrorTests {
    // 0362b3
    @Test
    public void unsuccessfulStartTransitionsSingleUnitToFailed() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        TestObserver<State> observer = expectedStateObservable(bus, "unit1", State.FAILED).test();

        Tracker tracker = new Tracker(bus);
        String unitID = "unit1";
        FakeUnit unit1 = new FakeUnit(unitID, bus);

        // WHEN
        bus.sink().accept(command(unitID, ENABLE));
        unit1.failStart();

        // THEN
        observer.awaitCount(1);
        assertTracker(tracker);
    }

    // 588954
    @Test
    public void unsuccessfulStartTransitionsIndependentUnitsToFailed() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        TestObserver<Boolean> testObserver = expectedStatesObservable(bus, "unit1", State.FAILED, "unit2", State.FAILED).test();

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
        testObserver
                .awaitCount(1);
        assertTracker(tracker);
    }

    // f859a1
    @Test
    public void unsuccessfulDependentStartTransitionsItToFailedAndDependencyToStarted() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        TestObserver<Boolean> testObserver = expectedStatesObservable(bus, "unit1", State.FAILED, "unit2", State.STARTED).test();
        Tracker tracker = new Tracker(bus);
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
        testObserver.awaitCount(1);
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
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        unit1.addDependency(unit2ID);

        // WHEN
        bus.sink().accept(command(unit1ID, START));
        unit2.failStart();
        unit1.completeStart();

        // THEN
        assertTracker(tracker);
    }

    // ca9c14
    @Test
    public void unsuccessfulDependencyStartTransitionsItToFailedAndDependentToStartingAndOtherDependencySarted() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
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
        assertTracker(tracker);
    }

    // b35bc4
    @Test
    public void unsuccessfulTransitiveDependencyStartTransitionsItToFailedAndDependentsToStarting() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
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
        assertTracker(tracker);
    }

    // cd5c29
    @Test
    public void unsuccessfulStopTransitionsSingleUnitToFailed() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        String unitID = "unit1";
        FakeUnit unit1 = new FakeUnit(unitID, bus);

        // WHEN
        bus.sink().accept(command(unitID, START));
        unit1.completeStart();
        bus.sink().accept(command(unitID, STOP));
        unit1.failStop();

        // THEN
        assertTracker(tracker);
    }

    // 4783e6
    @Test
    public void unsuccessfulStopTransitionsIndependentUnitsToFailed() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
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
        assertTracker(tracker);
    }

    // 5d4caf
    @Test
    public void unsuccessfulStopTransitionsDependencyToFailedAndDependentToStopped() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
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
//        testTransitionObserver
//                .awaitCount(6); // make sure that START command arrives earlier than STOP

        // WHEN
        bus.sink().accept(command(unit2ID, STOP));
        unit2.failStop();
        unit1.completeStop();

        // THEN
//        testTransitionObserver
//                .awaitCount(10);
        assertTracker(tracker);
    }

    // 87402f
    @Test
    public void unsuccessfulTransitiveDependencyStopTransitionsItToFailedAndDependentsToStopped() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
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
//        testTransitionObserver
//                .awaitCount(10); // make sure that START command arrives earlier than STOP

        // WHEN
        bus.sink().accept(command(unit3ID, STOP));
        unit3.failStop();
        unit2.completeStop();
        unit1.completeStop();

        // THEN
//        testTransitionObserver
//                .awaitCount(16);
        assertTracker(tracker);
    }


    private Message<Command> command(String targetUnitId, Command start) {
        return message(Command.class)
                .withTarget(targetUnitId)
                .withPayload(start);
    }

    private Observable<Boolean> expectedStatesObservable(Bus bus,
                                                         String unitId1, State expectedState1,
                                                         String unitId2, State expectedState2) {
        return Observable.combineLatest(
                expectedStateObservable(bus, unitId1, expectedState1),
                expectedStateObservable(bus, unitId2, expectedState2), (a, b) -> true);
    }

    private Observable<State> expectedStateObservable(Bus bus, String unitId, State expectedState) {
        return bus.events()
                .filter(it -> it.messageType().equals(Transition.class))
                .map((Function<Message, Optional>) Message::payload)
                .cast(Transition.class)
                .filter(transition -> transition.unitId().equals(unitId))
                .filter(transition -> transition.current() == expectedState)
                .map(Transition::current);
    }
}
