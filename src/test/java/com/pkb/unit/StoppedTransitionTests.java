package com.pkb.unit;

import static com.pkb.unit.DesiredState.DISABLED;
import static com.pkb.unit.DesiredState.ENABLED;
import static com.pkb.unit.DesiredState.UNSET;
import static com.pkb.unit.State.FAILED;
import static com.pkb.unit.State.STARTED;
import static com.pkb.unit.State.STARTING;
import static com.pkb.unit.State.STOPPED;
import static com.pkb.unit.tracker.ImmutableSystemState.systemState;
import static com.pkb.unit.tracker.ImmutableUnit.unit;

import org.junit.Test;

public class StoppedTransitionTests extends AbstractUnitTest {

    // I/1
    @Test
    public void whenEnableStoppedAndCompleteStartThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit unit = new FakeUnit("unit1", bus);
        unit.enable();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTING).withDesiredState(ENABLED))
                .build());

        // WHEN
        unit.completeStart();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());
    }

    // I/2
    @Test
    public void whenEnableStoppedAndFailStartAndStartRetryThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.enable();
        fakeUnit.failStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(ENABLED).withState(FAILED))
                .build());

        // WHEN
        testComputationScheduler.advanceTimeBy(FakeUnit.RETRY_PERIOD + 1, FakeUnit.RETRY_PERIOD_UNIT);

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(ENABLED).withState(STARTING))
                .build());
    }

    // I/3
    @Test
    public void whenStartStoppedDisabledThenStopped() throws Exception {
        // GIVEN
        setupComputationTestScheduler();

        // WHEN
        FakeUnit unit = new FakeUnit("unit1", bus);
        unit.disable();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STOPPED).withDesiredState(DISABLED))
                .build());

        // WHEN
        unit.start();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STOPPED).withDesiredState(DISABLED))
                .build());
    }

    // I/4
    @Test
    public void whenStartStoppedUnsetAndCompleteStartThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit unit = new FakeUnit("unit1", bus);
        unit.start();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTING).withDesiredState(UNSET))
                .build());

        // WHEN
        unit.completeStart();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());
    }

    // I/5
    @Test
    public void whenStartStoppedUnsetAndFailStartThenFailed() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit unit = new FakeUnit("unit1", bus);
        unit.start();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTING).withDesiredState(UNSET))
                .build());

        // WHEN
        unit.failStart();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());
    }

    // I/6
    @Test
    public void whenDisableStoppedThenStoppedDisabled() throws Exception {
        // GIVEN
        setupComputationTestScheduler();

        // WHEN
        FakeUnit unit = new FakeUnit("unit1", bus);
        unit.disable();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STOPPED).withDesiredState(DISABLED))
                .build());
    }

    // I/7
    @Test
    public void whenStopStoppedDisabledThenStopped() throws Exception {
        // GIVEN
        setupComputationTestScheduler();

        // WHEN
        FakeUnit unit = new FakeUnit("unit1", bus);
        unit.disable();
        unit.stop();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STOPPED).withDesiredState(DISABLED))
                .build());
    }

    // I/8
    @Test
    public void whenStopStoppedUnsetThenStopped() throws Exception {
        // GIVEN
        setupComputationTestScheduler();

        // WHEN
        FakeUnit unit = new FakeUnit("unit1", bus);
        unit.stop();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STOPPED).withDesiredState(UNSET))
                .build());
    }

    // I/9
    @Test
    public void whenClearDesiredStateStoppedThenStoppedUnset() throws Exception {
        // GIVEN
        setupComputationTestScheduler();

        // WHEN
        FakeUnit unit = new FakeUnit("unit1", bus);
        unit.clearDesiredState();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STOPPED).withDesiredState(UNSET))
                .build());
    }

    // II/1
    @Test
    public void whenEnableStoppedAndEnableStartingThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit unit = new FakeUnit("unit1", bus);
        unit.enable();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTING).withDesiredState(ENABLED))
                .build());

        // WHEN
        unit.enable();
        unit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());
    }

    // II/2
    @Test
    public void whenEnableStoppedAndStartStartingThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit unit = new FakeUnit("unit1", bus);
        unit.enable();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTING).withDesiredState(ENABLED))
                .build());

        // WHEN
        unit.start();
        unit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());
    }

    // II/3
    @Test
    public void whenEnableStoppedAndDisableStartingAndCompleteStartAndStopRetryStartedThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit unit = new FakeUnit("unit1", bus);
        unit.enable();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTING).withDesiredState(ENABLED))
                .build());

        // WHEN
        unit.disable();
        unit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTED).withDesiredState(DISABLED))
                .build());

        // WHEN
        testComputationScheduler.advanceTimeBy(FakeUnit.RETRY_PERIOD, FakeUnit.RETRY_PERIOD_UNIT);
        unit.completeStop();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(DISABLED))
                .build());
    }

    // II/4
    @Test
    public void whenEnableStoppedAndStopStartingThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit unit = new FakeUnit("unit1", bus);
        unit.enable();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTING).withDesiredState(ENABLED))
                .build());

        // WHEN
        unit.stop();
        unit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());

        // WHEN
        testComputationScheduler.advanceTimeBy(FakeUnit.RETRY_PERIOD, FakeUnit.RETRY_PERIOD_UNIT);
        unit.completeStop(); // not needed in expected case and remains STARTED
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());
    }

    // II/5
    @Test
    public void whenEnableStoppedAndClearDesiredStateStartingThenStartedUnset() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit unit = new FakeUnit("unit1", bus);
        unit.enable();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTING).withDesiredState(ENABLED))
                .build());

        // WHEN
        unit.clearDesiredState();
        unit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());
    }

    // I/6
    @Test
    public void whenStartStoppedAndEnableStartingThenStartedEnabled() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit unit = new FakeUnit("unit1", bus);
        unit.start();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTING).withDesiredState(UNSET))
                .build());
        // WHEN
        unit.enable();
        unit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());
    }

    // I/7
    @Test
    public void whenStartStoppedAndStartStartingThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit unit = new FakeUnit("unit1", bus);
        unit.start();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTING).withDesiredState(UNSET))
                .build());
        // WHEN
        unit.start();
        unit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());
    }

    // I/8
    @Test
    public void whenStartStoppedAndDisableStartingAndStopRetryStartedThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit unit = new FakeUnit("unit1", bus);
        unit.start();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTING).withDesiredState(UNSET))
                .build());
        // WHEN
        unit.disable();
        unit.completeStart();
        unit.completeStop(); // STOP of DISABLE command will be ignored until START has been finished
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STARTED).withDesiredState(DISABLED))
                .build());

        // WHEN
        testComputationScheduler.advanceTimeBy(FakeUnit.RETRY_PERIOD, FakeUnit.RETRY_PERIOD_UNIT); // retry logic sends STOP
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(DISABLED))
                .build());
    }

    // II/9
    @Test
    public void whenStartStoppedAndStopStartingThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        unit1.start();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTING).withDesiredState(UNSET))
                .build());

        // WHEN
        unit1.stop();
        unit1.completeStart();
        unit1.completeStop(); // will be ignored
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());
    }

    // II/10
    @Test
    public void whenStartStoppedAndClearDesiredStateStartingThenStartedUnset() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        unit1.start();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTING).withDesiredState(UNSET))
                .build());

        // WHEN
        unit1.clearDesiredState();
        unit1.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());
    }
}
