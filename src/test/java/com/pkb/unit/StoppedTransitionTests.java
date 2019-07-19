package com.pkb.unit;

import static com.pkb.unit.Command.CLEAR_DESIRED_STATE;
import static com.pkb.unit.Command.DISABLE;
import static com.pkb.unit.Command.ENABLE;
import static com.pkb.unit.Command.START;
import static com.pkb.unit.Command.STOP;
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
        bus.sink().accept(command("unit1", ENABLE));
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
        bus.sink().accept(command("unit1", ENABLE));
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
        bus.sink().accept(command("unit1", DISABLE));
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STOPPED).withDesiredState(DISABLED))
                .build());

        // WHEN
        bus.sink().accept(command("unit1", START));
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
        bus.sink().accept(command("unit1", START));
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
        bus.sink().accept(command("unit1", START));
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
        bus.sink().accept(command("unit1", DISABLE));
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
        bus.sink().accept(command("unit1", DISABLE));
        bus.sink().accept(command("unit1", STOP));
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
        bus.sink().accept(command("unit1", STOP));
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
        bus.sink().accept(command("unit1", CLEAR_DESIRED_STATE));
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState()
                .addUnits(unit("unit1").withState(STOPPED).withDesiredState(UNSET))
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
        bus.sink().accept(command("unit1", START));
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTING).withDesiredState(UNSET))
                .build());

        // WHEN
        bus.sink().accept(command("unit1", STOP));
        unit1.completeStart();
        unit1.completeStop(); // will be ignored
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET))
                .build());
    }
}
