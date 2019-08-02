package com.pkb.unit.tracker;

import static com.pkb.unit.DesiredState.ENABLED;
import static com.pkb.unit.State.STARTED;
import static com.pkb.unit.tracker.ImmutableSystemState.systemState;
import static com.pkb.unit.tracker.ImmutableUnit.unit;

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
}
