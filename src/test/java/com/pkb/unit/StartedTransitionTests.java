package com.pkb.unit;

import static com.pkb.unit.DesiredState.DISABLED;
import static com.pkb.unit.DesiredState.ENABLED;
import static com.pkb.unit.DesiredState.UNSET;
import static com.pkb.unit.State.FAILED;
import static com.pkb.unit.State.STARTED;
import static com.pkb.unit.State.STOPPED;
import static com.pkb.unit.State.STOPPING;
import static com.pkb.unit.tracker.ImmutableSystemState.systemState;
import static com.pkb.unit.tracker.ImmutableUnit.unit;

import org.junit.Test;

public class StartedTransitionTests extends AbstractUnitTest {

    // I/1
    @Test
    public void whenEnableStartedThenStartedEnabled() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        unit1.start();
        unit1.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());

        // WHEN
        unit1.enable();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());
    }

    // I/2
    @Test
    public void whenStartStartedEnabledThenStarted() throws Exception {
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
        unit1.start();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());
    }

    // I/3
    @Test
    public void whenStartStartedUnsetThenStarted() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        unit1.start();
        unit1.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());

        // WHEN
        unit1.start();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());
    }

    // I/4
    @Test
    public void whenDisableStartedAndCompleteStopStoppingThenStopped() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.enable();
        fakeUnit.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.disable();
        fakeUnit.completeStop();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(DISABLED))
                .build());
    }

    // I/5
    @Test
    public void whenDisableStartedAndFailStopStoppingThenFailed() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.enable();
        fakeUnit.completeStart();
        testScheduler.triggerActions();
        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.disable();
        fakeUnit.failStop();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(DISABLED))
                .build());
    }

    // I/6
    @Test
    public void whenStopStartedEnabledAndCompleteStopAndStartSelfThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.enable();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.stop();
        fakeUnit.completeStop();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.completeStart();
        // processes START sent by the Unit itself at the and of stopping. No retry needed.
        testComputationScheduler.triggerActions();
        // processes completeStart
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());
    }

    // I/7
    @Test
    public void whenStopStartedEnabledAndFailStopAndStartSelfThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.enable();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.stop();
        fakeUnit.failStop();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.completeStart();
        // processes START sent by the Unit itself at the and of stopping. No retry needed.
        testComputationScheduler.triggerActions();
        // processes completeStart
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());
    }

    // I/8
    @Test
    public void whenStopStartedUnsetAndCompleteStopThenStopped() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.start();
        fakeUnit.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.stop();
        fakeUnit.completeStop();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(UNSET))
                .build());
    }

    // I/9
    @Test
    public void whenStopStartedUnsetAndFailStopThenFailed() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.start();
        fakeUnit.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.stop();
        fakeUnit.failStop();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET))
                .build());
    }

    // I/10
    @Test
    public void whenClearDesiredStateStartedThenStartedUnset() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.enable();
        fakeUnit.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.clearDesiredState();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());
    }

    // II/1
    @Test
    public void whenDisableStartedAndEnableStoppingAndCompleteStopAndStartRetrySelfThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.start();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.disable();
        fakeUnit.completeStop();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPING).withDesiredState(DISABLED))
                .build());

        // WHEN
        fakeUnit.enable();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(ENABLED))
                .build());

        // WHEN
        testComputationScheduler.triggerActions(); // processes self retry sent after completeStop
        testIOScheduler.triggerActions(); // process completeStart

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());
    }

    // II/2
    @Test
    public void whenDisableStartedAndStartStoppingAndCompleteStopThenStoppedDisabled() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.start();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.disable();
        fakeUnit.completeStop();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPING).withDesiredState(DISABLED))
                .build());

        // WHEN
        fakeUnit.start();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(DISABLED))
                .build());

        // WHEN
        // make sure Unit is not restarted via retry logic
        testComputationScheduler.advanceTimeBy(FakeUnit.RETRY_PERIOD, FakeUnit.RETRY_PERIOD_UNIT);
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(DISABLED))
                .build());
    }

    // II/3
    @Test
    public void whenDisableStartedAndDisableStoppingAndCompleteStopThenStopped() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.start();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.disable();
        fakeUnit.completeStop();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPING).withDesiredState(DISABLED))
                .build());

        // WHEN
        fakeUnit.disable();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(DISABLED))
                .build());
    }

    // II/4
    @Test
    public void whenDisableStartedAndStopStoppingAndCompleteStopThenStopped() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.start();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.disable();
        fakeUnit.completeStop();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPING).withDesiredState(DISABLED))
                .build());

        // WHEN
        fakeUnit.stop();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();
        fakeUnit.completeStart();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(DISABLED))
                .build());
    }

    // II/5
    @Test
    public void whenDisableStartedAndClearDesiredStateStoppingAndCompleteStopThenStoppedUnset() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.start();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());

        // WHEN
        fakeUnit.disable();
        fakeUnit.completeStop();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPING).withDesiredState(DISABLED))
                .build());

        // WHEN
        fakeUnit.clearDesiredState();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();
        fakeUnit.completeStart();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(UNSET))
                .build());
    }

    // I/6
    @Test
    public void whenStopStartedEnabledAndEnableStoppingAndCompleteStopAndStartRetrySelfStoppedThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.enable();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.stop();
        fakeUnit.completeStop();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPING).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.enable();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(ENABLED))
                .build());

        // WHEN
        testComputationScheduler.triggerActions(); // processes self retry START after completeStart
        testIOScheduler.triggerActions(); // processes completeStart

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());
    }

    // I/7
    @Test
    public void whenStopStartedEnabledAndStartStoppingAndCompleteStopAndStartRetrySelfThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.enable();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.stop();
        fakeUnit.completeStop();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPING).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.start();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(ENABLED))
                .build());

        // WHEN
        testComputationScheduler.triggerActions(); // processes self retry START after completeStart
        testIOScheduler.triggerActions(); // processes completeStart

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());
    }

    // I/8
    @Test
    public void whenStopStartedEnabledAndDisableStoppingAndCompleteStopThenStopped() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.enable();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.stop();
        fakeUnit.completeStop();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPING).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.disable();
        fakeUnit.completeStop();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(DISABLED))
                .build());

        // WHEN
        // make sure Unit is not restarted via retry logic
        testComputationScheduler.advanceTimeBy(FakeUnit.RETRY_PERIOD, FakeUnit.RETRY_PERIOD_UNIT);
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(DISABLED))
                .build());
    }

    // I/9
    @Test
    public void whenStopStartedEnabledAndStopStoppingAndCompleteStopAndStartRetrySelfThenStarted() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.enable();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.stop();
        fakeUnit.completeStop();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPING).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.stop();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions(); // processes self retry START after completeStart
        testIOScheduler.triggerActions(); // processes completeStart

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());
    }

    // I/10
    @Test
    public void whenStopStartedEnabledAndClearDesiredStateStoppingAndCompleteStopThenStoppedUnset() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.enable();
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.stop();
        fakeUnit.completeStop();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPING).withDesiredState(ENABLED))
                .build());

        // WHEN
        fakeUnit.clearDesiredState();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(UNSET))
                .build());
    }

    // II/11
    @Test
    public void whenStopStartedUnsetAndEnableStoppingAndCompleteStopAndStartRetryThenStoppedEnabled() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        unit1.start();
        unit1.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());

        // WHEN
        unit1.stop();
        unit1.completeStop();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPING).withDesiredState(UNSET))
                .build());

        // WHEN
        unit1.enable();
        unit1.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(ENABLED))
                .build());

        // WHEN
        testComputationScheduler.triggerActions(); // processes self retry START after completeStart
        testIOScheduler.triggerActions(); // processes completeStart

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED))
                .build());
    }

    // II/12
    @Test
    public void whenStopStartedUnsetAndStartStoppingThenStopped() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        unit1.start();
        unit1.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());

        // WHEN
        unit1.stop();
        unit1.completeStop();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPING).withDesiredState(UNSET))
                .build());

        // WHEN
        unit1.start();
        unit1.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(UNSET))
                .build());
    }

    // II/13
    @Test
    public void whenStopStartedUnsetAndDisableStoppingThenStoppedDisabled() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        unit1.start();
        unit1.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());

        // WHEN
        unit1.stop();
        unit1.completeStop();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPING).withDesiredState(UNSET))
                .build());

        // WHEN
        unit1.disable();
        unit1.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(DISABLED))
                .build());
    }

    // II/14
    @Test
    public void whenStopStartedUnsetAndStopStoppingThenStopped() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        unit1.start();
        unit1.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());

        // WHEN
        unit1.stop();
        unit1.completeStop();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPING).withDesiredState(UNSET))
                .build());

        // WHEN
        unit1.stop();
        unit1.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(UNSET))
                .build());
    }

    // II/15
    @Test
    public void whenStopStartedUnsetAndClearDesiredStateStoppingThenStopped() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        unit1.start();
        unit1.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());

        // WHEN
        unit1.stop();
        unit1.completeStop();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPING).withDesiredState(UNSET))
                .build());

        // WHEN
        unit1.clearDesiredState();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(UNSET))
                .build());
    }
}
