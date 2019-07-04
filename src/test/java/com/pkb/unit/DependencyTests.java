package com.pkb.unit;

import static com.pkb.unit.TestCommon.assertTracker;
import static com.pkb.unit.message.ImmutableMessage.message;

import org.junit.After;
import org.junit.Test;

import com.pkb.unit.message.Message;
import com.pkb.unit.message.payload.Transition;

import io.reactivex.observers.TestObserver;

// 145d08
public class DependencyTests {

    private TestObserver<Message> testTransitionObserver;

    // fe7390
    @Test
    public void testAddOneDependency() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        testTransitionObserver = testTransitionObserver(bus);

        // WHEN
        unit1.addDependency(unit2ID);

        testTransitionObserver
                .awaitCount(1); // Should see 1 transition

        // THEN
        assertTracker(tracker);
    }

    // 93d1e2
    @Test
    public void testAddMoreDependencies() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        String unit4ID = "unit4";
        String unit5ID = "unit5";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        FakeUnit unit4 = new FakeUnit(unit4ID, bus);
        FakeUnit unit5 = new FakeUnit(unit5ID, bus);
        testTransitionObserver = testTransitionObserver(bus);

        // WHEN
        unit1.addDependency(unit2ID);
        unit1.addDependency(unit3ID);
        unit1.addDependency(unit4ID);
        unit1.addDependency(unit5ID);

        testTransitionObserver
                .awaitCount(4);

        // THEN
        assertTracker(tracker);
    }

    // 0bb308
    @Test
    public void testAddOneTransitiveDependency() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        testTransitionObserver = testTransitionObserver(bus);

        // WHEN
        unit1.addDependency(unit2ID);
        unit2.addDependency(unit3ID);

        testTransitionObserver
                .awaitCount(2); // Should see 1 transition

        // THEN
        assertTracker(tracker);
    }

    // 5820cc
    @Test
    public void testAddMoreTransitiveDependencies() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        String unit4ID = "unit4";
        String unit5ID = "unit5";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        FakeUnit unit4 = new FakeUnit(unit4ID, bus);
        FakeUnit unit5 = new FakeUnit(unit5ID, bus);
        testTransitionObserver = testTransitionObserver(bus);

        // WHEN
        unit1.addDependency(unit2ID);
        unit2.addDependency(unit3ID);
        unit2.addDependency(unit4ID);
        unit2.addDependency(unit5ID);

        testTransitionObserver
                .awaitCount(4); // Should see 1 transition

        // THEN
        assertTracker(tracker);
    }

    // 5a9f25
    @Test
    public void testAddTwoDepthTransitiveDependency() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        String unit4ID = "unit4";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        FakeUnit unit4 = new FakeUnit(unit4ID, bus);
        testTransitionObserver = testTransitionObserver(bus);

        // WHEN
        unit1.addDependency(unit2ID);
        unit2.addDependency(unit3ID);
        unit3.addDependency(unit4ID);

        testTransitionObserver
                .awaitCount(3); // Should see 1 transition

        // THEN
        assertTracker(tracker);
    }

    // 4eb86c
    @Test
    public void testAddMoreTwoDepthTransitiveDependencies() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        String unit4ID = "unit4";
        String unit5ID = "unit5";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        FakeUnit unit4 = new FakeUnit(unit4ID, bus);
        FakeUnit unit5 = new FakeUnit(unit5ID, bus);
        testTransitionObserver = testTransitionObserver(bus);

        // WHEN
        unit1.addDependency(unit2ID);
        unit2.addDependency(unit3ID);
        unit3.addDependency(unit4ID);
        unit3.addDependency(unit5ID);

        testTransitionObserver
                .awaitCount(4); // Should see 1 transition

        // THEN
        assertTracker(tracker);
    }

    // 2e2f50
    @Test
    public void testAddDependenciesToDifferentDependencyTrees() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        Tracker tracker = new Tracker(bus);
        String unit1aID = "unit1a";
        String unit2aID = "unit2a";
        String unit3aID = "unit3a";
        String unit1bID = "unit1b";
        String unit2bID = "unit2b";
        String unit3bID = "unit3b";
        FakeUnit unit1a = new FakeUnit(unit1aID, bus);
        FakeUnit unit2a = new FakeUnit(unit2aID, bus);
        FakeUnit unit3a = new FakeUnit(unit3aID, bus);
        FakeUnit unit1b = new FakeUnit(unit1bID, bus);
        FakeUnit unit2b = new FakeUnit(unit2bID, bus);
        FakeUnit unit3b = new FakeUnit(unit3bID, bus);
        testTransitionObserver = testTransitionObserver(bus);

        // WHEN
        unit1a.addDependency(unit2aID);
        unit1a.addDependency(unit3aID);
        unit1b.addDependency(unit2bID);
        unit1b.addDependency(unit3bID);

        testTransitionObserver
                .awaitCount(4); // Should see 1 transition

        // THEN
        assertTracker(tracker);
    }

    @After
    public void disposeTestObserver() {
        testTransitionObserver.dispose();
    }

    // utility
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
