package com.pkb.unit.tracker;

import static com.pkb.unit.DesiredState.ENABLED;
import static com.pkb.unit.DesiredState.UNSET;
import static com.pkb.unit.State.FAILED;
import static com.pkb.unit.State.STARTED;
import static com.pkb.unit.tracker.ImmutableSystemState.systemState;
import static com.pkb.unit.tracker.ImmutableUnit.unit;

import org.junit.Ignore;
import org.junit.Test;

import com.pkb.unit.AbstractUnitTest;
import com.pkb.unit.FakeUnit;

import io.reactivex.observers.TestObserver;

public class TrackerRestartTest extends AbstractUnitTest {

    @Test
    public void restartTrackerEmitsWhenUnitIsRestarted() {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        unit1.enable();
        unit1.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());

        // WHEN
        TestObserver<Boolean> testRestartedObserver = Tracker.unitRestarted(bus, "unit1").test();
        unit1.stop();
        unit1.completeStop();
        unit1.start();
        unit1.completeStart();
        testScheduler.triggerActions();

        // THEN
        testRestartedObserver.assertResult(true);
    }

    @Test
    public void restartTrackerDoesntEmitWhenUnitFailsToRestart() {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        unit1.enable();
        unit1.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());

        // WHEN
        TestObserver<Boolean> testRestartedObserver = Tracker.unitRestarted(bus, "unit1").test();
        unit1.stop();
        unit1.completeStop();
        unit1.start();
        unit1.failStart();
        testScheduler.triggerActions();

        // THEN
        testRestartedObserver.assertEmpty();
    }

    @Test
    public void restartTrackerSubscribesToCorrectTarget() {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        String unit1id = "unit1";
        FakeUnit unit1 = new FakeUnit(unit1id, bus);
        String unit2id = "unit2";
        FakeUnit unit2 = new FakeUnit(unit2id, bus);
        unit1.addDependency(unit2id);
        unit1.enable();
        unit2.completeStart();
        unit1.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit(unit1id).withState(STARTED).withDesiredState(ENABLED).withDependencies(unit2id),
                unit(unit2id).withState(STARTED).withDesiredState(UNSET))
                .build());

        // WHEN
        TestObserver<Boolean> testRestartedObserver = Tracker.unitRestarted(bus, unit1id).test();
        unit2.stop();
        unit2.completeStop();
        unit1.completeStop();
        unit2.start();
        unit1.failStart();
        testScheduler.triggerActions();

        // THEN
        testRestartedObserver.awaitCount(0);
        testRestartedObserver.assertNotComplete();
        testRestartedObserver.assertValueCount(0);
        assertLatestState(systemState().addUnits(
                unit(unit1id).withState(FAILED).withDesiredState(ENABLED).withDependencies(unit2id),
                unit(unit2id).withState(STARTED).withDesiredState(UNSET))
                .build());

    }
}
