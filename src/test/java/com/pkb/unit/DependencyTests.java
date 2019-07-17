package com.pkb.unit;

import org.junit.After;
import org.junit.Test;

import com.pkb.unit.message.Message;
import com.pkb.unit.message.payload.Dependencies;
import com.pkb.unit.message.payload.Transition;

import io.reactivex.observers.TestObserver;

// 145d08
public class DependencyTests {

    private TestObserver<Message> testTransitionObserver;
    private TestObserver<Message> testDependencyObserver;

    // fe7390
    @Test
    public void testAddOneDependency() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        //Tracker tracker = new Tracker(bus);
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
        //assertTracker(tracker);
    }

    // 93d1e2
    @Test
    public void testAddMoreDependencies() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        //Tracker tracker = new Tracker(bus);
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
        //assertTracker(tracker);
    }

    // 0bb308
    @Test
    public void testAddOneTransitiveDependency() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        //Tracker tracker = new Tracker(bus);
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
                .awaitCount(2);

        // THEN
        //assertTracker(tracker);
    }

    // 5820cc
    @Test
    public void testAddMoreTransitiveDependencies() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        //Tracker tracker = new Tracker(bus);
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
                .awaitCount(4);

        // THEN
        //assertTracker(tracker);
    }

    // 5a9f25
    @Test
    public void testAddTwoDepthTransitiveDependency() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        //Tracker tracker = new Tracker(bus);
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
                .awaitCount(3);

        // THEN
        //assertTracker(tracker);
    }

    // 4eb86c
    @Test
    public void testAddMoreTwoDepthTransitiveDependencies() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        //Tracker tracker = new Tracker(bus);
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
                .awaitCount(4);

        // THEN
        //assertTracker(tracker);
    }

    // 2e2f50
    @Test
    public void testAddDependenciesToDifferentDependencyTrees() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        //Tracker tracker = new Tracker(bus);
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
                .awaitCount(4);

        // THEN
        //assertTracker(tracker);
    }

    // fc37f4
    @Test
    public void testRemoveOneDependency() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        //Tracker tracker = new Tracker(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        testTransitionObserver = testTransitionObserver(bus);
        unit1.addDependency(unit2ID);
        testTransitionObserver
                .awaitCount(1); // should see 1 transition because a Unit.addDependency requests a report
        testDependencyObserver = testDependencyObserver(bus);

        // WHEN
        unit1.removeDependency(unit2ID);
        testDependencyObserver
                .awaitCount(1); // Should see 1 dependeny report

        // THEN
        //assertTracker(tracker);
    }

    // bb26eb
    @Test
    public void testRemoveMoreDependencies() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        //Tracker tracker = new Tracker(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        String unit4ID = "unit4";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        FakeUnit unit4 = new FakeUnit(unit4ID, bus);
        testTransitionObserver = testTransitionObserver(bus);
        unit1.addDependency(unit2ID);
        unit1.addDependency(unit3ID);
        unit1.addDependency(unit4ID);
        testTransitionObserver
                .awaitCount(3);
        testDependencyObserver = testDependencyObserver(bus);

        // WHEN
        unit1.removeDependency(unit2ID);
        unit1.removeDependency(unit3ID);
        unit1.removeDependency(unit4ID);
        testDependencyObserver
                .awaitCount(3);

        // THEN
        //assertTracker(tracker);
    }

    // 302aa0
    @Test
    public void testRemoveOneTransitiveDependency() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        //Tracker tracker = new Tracker(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        testTransitionObserver = testTransitionObserver(bus);
        unit1.addDependency(unit2ID);
        unit2.addDependency(unit3ID);
        testTransitionObserver
                .awaitCount(2);
        testDependencyObserver = testDependencyObserver(bus);

        // WHEN
        unit2.removeDependency(unit3ID);
        testDependencyObserver
                .awaitCount(1);

        // THEN
        //assertTracker(tracker);
    }

    // 0cc1e3
    @Test
    public void testRemoveMoreTransitiveDependencies() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        //Tracker tracker = new Tracker(bus);
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
        unit1.addDependency(unit2ID);
        unit2.addDependency(unit3ID);
        unit2.addDependency(unit4ID);
        unit2.addDependency(unit5ID);
        testTransitionObserver
                .awaitCount(4);
        testDependencyObserver = testDependencyObserver(bus);

        // WHEN
        unit2.removeDependency(unit5ID);
        unit2.removeDependency(unit4ID);
        unit2.removeDependency(unit3ID);
        testDependencyObserver
                .awaitCount(3);

        // THEN
        //assertTracker(tracker);
    }

    // 998ebe
    @Test
    public void testRemoveTwoDepthTransitiveDependency() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        //Tracker tracker = new Tracker(bus);
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        String unit4ID = "unit4";
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        FakeUnit unit4 = new FakeUnit(unit4ID, bus);
        testTransitionObserver = testTransitionObserver(bus);
        unit1.addDependency(unit2ID);
        unit2.addDependency(unit3ID);
        unit3.addDependency(unit4ID);
        testTransitionObserver
                .awaitCount(3);
        testDependencyObserver = testDependencyObserver(bus);

        // WHEN
        unit3.removeDependency(unit4ID);
        testDependencyObserver
                .awaitCount(1);

        // THEN
        //assertTracker(tracker);
    }

    // 3ba6ad
    @Test
    public void testRemoveMoreTwoDepthTransitiveDependencies() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        //Tracker tracker = new Tracker(bus);
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
        unit1.addDependency(unit2ID);
        unit2.addDependency(unit3ID);
        unit3.addDependency(unit4ID);
        unit3.addDependency(unit5ID);
        testTransitionObserver
                .awaitCount(4);
        testDependencyObserver = testDependencyObserver(bus);

        // WHEN
        unit3.removeDependency(unit5ID);
        unit3.removeDependency(unit4ID);
        testDependencyObserver
                .awaitCount(2);

        // THEN
        //assertTracker(tracker);
    }

    // b8288d
    @Test
    public void testRemoveDependenciesFromDifferentDependencyTrees() throws Exception {
        // GIVEN
        Bus bus = new LocalBus();
        //Tracker tracker = new Tracker(bus);
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
        unit1a.addDependency(unit2aID);
        unit1a.addDependency(unit3aID);
        unit1b.addDependency(unit2bID);
        unit1b.addDependency(unit3bID);
        testTransitionObserver
                .awaitCount(4);
        testDependencyObserver = testDependencyObserver(bus);

        // WHEN
        unit1a.removeDependency(unit2aID);
        unit1a.removeDependency(unit3aID);
        unit1b.removeDependency(unit2bID);
        testDependencyObserver
                .awaitCount(3);

        // THEN
        //assertTracker(tracker);
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
}
