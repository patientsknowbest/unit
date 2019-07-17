package com.pkb.unit;

import static com.pkb.unit.Command.CLEAR_DESIRED_STATE;
import static com.pkb.unit.Command.DISABLE;
import static com.pkb.unit.Command.ENABLE;
import static com.pkb.unit.Command.START;
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

public class RetryTests extends AbstractUnitTest {
    @Test
    public void noDesiredStateHasNoRetry() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        bus.sink().accept(command("unit1", START));
        fakeUnit.failStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(UNSET).withState(FAILED)
        ).build());

        // WHEN
        testScheduler.advanceTimeBy(FakeUnit.RETRY_PERIOD + 1, FakeUnit.RETRY_PERIOD_UNIT);

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(UNSET).withState(FAILED)
        ).build());
    }

    @Test
    public void enabledUnitRetries() throws Exception {
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
                unit("unit1").withDesiredState(ENABLED).withState(FAILED)
        ).build());

        // WHEN
        testComputationScheduler.advanceTimeBy(FakeUnit.RETRY_PERIOD + 1, FakeUnit.RETRY_PERIOD_UNIT);

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(ENABLED).withState(STARTING)
        ).build());
    }

    @Test
    public void disabledUnitStops() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        setupIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        bus.sink().accept(command("unit1", ENABLE));
        fakeUnit.completeStart();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(ENABLED).withState(STARTED)
        ).build());

        // WHEN
        bus.sink().accept(command("unit1", DISABLE));
        fakeUnit.completeStop();
        testComputationScheduler.triggerActions();
        testIOScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(DISABLED).withState(STOPPED)
        ).build());
    }

    @Test
    public void disabledUnitWontStart() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        new FakeUnit("unit1", bus);
        bus.sink().accept(command("unit1", DISABLE));
        bus.sink().accept(command("unit1", START));
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(DISABLED).withState(STOPPED)
        ).build());
    }

    @Test
    public void disabledReenableDependency() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        FakeUnit unit2 = new FakeUnit("unit2", bus);
        unit1.addDependency("unit2");
        bus.sink().accept(command("unit1", ENABLE));
        unit1.completeStart();
        unit2.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED).withDependencies("unit2"),
                unit("unit2").withState(STARTED).withDesiredState(UNSET)
        ).build());

        // WHEN
        bus.sink().accept(command("unit2", DISABLE));
        unit2.completeStop();
        unit1.completeStop();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTING).withDesiredState(ENABLED).withDependencies("unit2"),
                unit("unit2").withState(STOPPED).withDesiredState(DISABLED)
        ).build());

        // WHEN
        bus.sink().accept(command("unit2", CLEAR_DESIRED_STATE));
        unit1.completeStart();
        unit2.completeStart();
        testScheduler.advanceTimeBy(FakeUnit.RETRY_PERIOD, FakeUnit.RETRY_PERIOD_UNIT);

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED).withDependencies("unit2"),
                unit("unit2").withState(STARTED).withDesiredState(UNSET)
        ).build());
    }

    @Test
    public void spontaneousFailureRetries() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        bus.sink().accept(command("unit1", ENABLE));
        fakeUnit.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(ENABLED).withState(STARTED)
        ).build());

        // WHEN
        fakeUnit.failed();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(ENABLED).withState(FAILED)
        ).build());

        // WHEN
        fakeUnit.completeStart();
        testScheduler.advanceTimeBy(FakeUnit.RETRY_PERIOD + 1, FakeUnit.RETRY_PERIOD_UNIT);

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(ENABLED).withState(STARTED)
        ).build());
    }
}
