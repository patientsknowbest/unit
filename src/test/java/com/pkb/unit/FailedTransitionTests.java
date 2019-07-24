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

public class FailedTransitionTests extends AbstractUnitTest {

    // I/1
    @Test
    public void whenEnableFailedUnsetAndCompleteStartThenStartedEnabled() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.failed();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.enable();
        fakeUnit.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());
    }

    // I/2
    @Test
    public void whenEnableFailedUnsetAndFailStartAndStartRetryFailedThenStartedEnabled() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.failed();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.enable();
        fakeUnit.failStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.completeStart();
        testScheduler.advanceTimeBy(FakeUnit.RETRY_PERIOD, FakeUnit.RETRY_PERIOD_UNIT);

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());
    }

    // I/3
    @Test
    public void whenStartFailedDisabledThenFailed() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.failed();
        fakeUnit.disable();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(DISABLED))
                .build());

        // WHEN
        fakeUnit.disable();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(DISABLED))
                .build());
    }

    // I/4
    @Test
    public void whenStartFailedUnsetAndCompleteStartThenStarted() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.failed();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.start();
        fakeUnit.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());
    }

    // I/5
    @Test
    public void whenStartFailedUnsetAndFailStartThenFailed() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.failed();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.start();
        fakeUnit.failStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());

        // WHEN
        // Make sure UNSET unit is not restarted by rerty logic
        testScheduler.advanceTimeBy(FakeUnit.RETRY_PERIOD + 1, FakeUnit.RETRY_PERIOD_UNIT);

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());
    }

    // I/6
    @Test
    public void whenStopFailedDisabledThenFailed() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.failed();
        fakeUnit.disable();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(DISABLED))
                .build());

        // WHEN
        fakeUnit.stop();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(DISABLED))
                .build());
    }

    // I/7
    @Test
    public void whenDisableFailedThenFailedDisabled() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.failed();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.disable();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(DISABLED))
                .build());
    }

    // I/8
    @Test
    public void whenStopFailedThenFailed() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.failed();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.stop();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());
    }

    // I/9
    @Test
    public void whenClearDesiredStateFailedThenFailedUnset() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.failed();
        fakeUnit.disable();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(DISABLED))
                .build());

        // WHEN
        fakeUnit.clearDesiredState();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());
    }

    // II/1
    @Test
    public void whenEnableFailedAndEnableStartingThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.failed();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.enable();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTING).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.enable();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());
    }

    // II/2
    @Test
    public void whenEnableFailedAndStartStartingThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.failed();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.enable();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTING).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.start();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());
    }

    // II/3
    @Test
    public void whenEnableFailedAndDisableStartingAndCompleteStartAndStopRetryAndCompleteStopThenStoppedDisabled() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.failed();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.enable();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTING).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.disable();
        fakeUnit.completeStop();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(DISABLED))
                .build());

        // WHEN
        testComputationScheduler.advanceTimeBy(FakeUnit.RETRY_PERIOD, FakeUnit.RETRY_PERIOD_UNIT);
        fakeUnit.completeStop();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(DISABLED))
                .build());
    }

    // II/4
    @Test
    public void whenEnableFailedAndStopStartingThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.failed();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.enable();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTING).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.stop();
        fakeUnit.completeStop();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());
    }

    // II/5
    @Test
    public void whenEnableFailedAndClearDesiredStateStartingThenStartedUnset() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.failed();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.enable();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTING).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.clearDesiredState();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());
    }

    // II/6
    @Test
    public void whenStartFailedUnsetAndEnableStartingAndCompleteStartThenStartedEnabled() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.failed();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.start();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTING).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.enable();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());
    }

    // II/7
    @Test
    public void whenStartFailedUnsetAndStartStartingAndCompleteStartThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.failed();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.start();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTING).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.start();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());
    }

    // II/8
    @Test
    public void whenStartFailedUnsetAndDisableStartingAndCompleteStartAndStopRetryThenStopped() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.failed();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.start();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTING).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.disable();
        fakeUnit.completeStop();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(DISABLED))
                .build());

        // WHEN
        testComputationScheduler.advanceTimeBy(FakeUnit.RETRY_PERIOD, FakeUnit.RETRY_PERIOD_UNIT);
        fakeUnit.completeStop();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(DISABLED))
                .build());
    }

    // II/9
    @Test
    public void whenStartFailedUnsetAndStopStartingThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.failed();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.start();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTING).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.stop();
        fakeUnit.completeStop();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());
    }

    // II/10
    @Test
    public void whenStartFailedUnsetAndClearDesiredStateStartingThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.failed();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.start();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTING).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.clearDesiredState();
        fakeUnit.completeStop();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());
    }
}
