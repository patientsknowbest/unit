package com.pkb.unit;

import static com.pkb.unit.Command.ENABLE;
import static com.pkb.unit.Command.START;
import static com.pkb.unit.Command.STOP;
import static com.pkb.unit.DesiredState.ENABLED;
import static com.pkb.unit.DesiredState.UNSET;
import static com.pkb.unit.State.FAILED;
import static com.pkb.unit.State.STARTED;
import static com.pkb.unit.State.STARTING;
import static com.pkb.unit.State.STOPPED;
import static com.pkb.unit.tracker.ImmutableSystemState.systemState;
import static com.pkb.unit.tracker.ImmutableUnit.unit;

import org.junit.Test;

public class ErrorTests extends AbstractUnitTest {
    @Test
    public void unsuccessfulStartTransitionsSingleUnitToFailed() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        bus.sink().accept(command("unit1", ENABLE));
        unit1.failStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(unit("unit1").withDesiredState(ENABLED).withState(FAILED)).build());
    }

    @Test
    public void unsuccessfulStartTransitionsIndependentUnitsToFailed() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        FakeUnit unit2 = new FakeUnit("unit2", bus);
        bus.sink().accept(command("unit1", START));
        bus.sink().accept(command("unit2", START));
        unit1.failStart();
        unit2.failStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET),
                unit("unit2").withState(FAILED).withDesiredState(UNSET)
        ).build());
    }

    // f859a1
    @Test
    public void unsuccessfulDependentStartTransitionsItToFailedAndDependencyToStarted() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        FakeUnit unit2 = new FakeUnit("unit2", bus);
        unit1.addDependency("unit2");
        bus.sink().accept(command("unit1", ENABLE));
        unit1.failStart();
        unit2.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(ENABLED).withState(FAILED).withDependencies("unit2"),
                unit("unit2").withDesiredState(UNSET).withState(STARTED)
        ).build());
    }

    @Test
    public void unsuccessfulDependencyStartTransitionsDependencyToFailedAndDependentToStarting() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // wHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        FakeUnit unit2 = new FakeUnit("unit2", bus);
        unit1.addDependency("unit2");
        bus.sink().accept(command("unit1", START));
        unit2.failStart();
        unit1.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDependencies("unit2").withDesiredState(UNSET).withState(STARTING),
                unit("unit2").withDesiredState(UNSET).withState(FAILED)
        ).build());
    }

    @Test
    public void unsuccessfulDependencyStartTransitionsItToFailedAndDependentToStartingAndOtherDependencySarted() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();
        
        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        FakeUnit unit2 = new FakeUnit("unit2", bus);
        FakeUnit unit3 = new FakeUnit("unit3", bus);
        unit1.addDependency("unit2");
        unit1.addDependency("unit3");
        bus.sink().accept(command("unit1", START));
        unit2.failStart();
        unit3.completeStart();

        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTING).withDesiredState(UNSET).withDependencies("unit2", "unit3"),
                unit("unit2").withState(FAILED).withDesiredState(UNSET),
                unit("unit3").withState(STARTED).withDesiredState(UNSET)
        ).build());
    }

    // b35bc4
    @Test
    public void unsuccessfulTransitiveDependencyStartTransitionsItToFailedAndDependentsToStarting() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        FakeUnit unit2 = new FakeUnit("unit2", bus);
        FakeUnit unit3 = new FakeUnit("unit3", bus);
        unit1.addDependency("unit2");
        unit2.addDependency("unit3");
        bus.sink().accept(command("unit1", START));
        unit3.failStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTING).withDesiredState(UNSET).withDependencies("unit2"),
                unit("unit2").withState(STARTING).withDesiredState(UNSET).withDependencies("unit3"),
                unit("unit3").withState(FAILED).withDesiredState(UNSET)
        ).build());
    }

    // cd5c29
    @Test
    public void unsuccessfulStopTransitionsSingleUnitToFailed() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        bus.sink().accept(command("unit1", START));
        unit1.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET)
        ).build());

        // WHEN
        bus.sink().accept(command("unit1", STOP));
        unit1.failStop();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET)
        ).build());
    }

    // 4783e6
    @Test
    public void unsuccessfulStopTransitionsIndependentUnitsToFailed() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        FakeUnit unit2 = new FakeUnit("unit2", bus);
        bus.sink().accept(command("unit1", START));
        bus.sink().accept(command("unit2", START));
        unit1.completeStart();
        unit2.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET),
                unit("unit2").withState(STARTED).withDesiredState(UNSET)
        ).build());

        // WHEN
        bus.sink().accept(command("unit1", STOP));
        bus.sink().accept(command("unit2", STOP));
        unit1.failStop();
        unit2.failStop();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(FAILED).withDesiredState(UNSET),
                unit("unit2").withState(FAILED).withDesiredState(UNSET)
        ).build());
    }

    @Test
    public void unsuccessfulStopTransitionsDependencyToFailedAndDependentToStopped() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        FakeUnit unit2 = new FakeUnit("unit2", bus);
        unit1.addDependency("unit2");
        bus.sink().accept(command("unit1", START));
        unit1.completeStart();
        unit2.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET).withDependencies("unit2"),
                unit("unit2").withState(STARTED).withDesiredState(UNSET)
        ).build());

        // WHEN
        bus.sink().accept(command("unit2", STOP));
        unit2.failStop();
        unit1.completeStop();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(UNSET).withDependencies("unit2"),
                unit("unit2").withState(FAILED).withDesiredState(UNSET)
        ).build());
    }

    @Test
    public void unsuccessfulTransitiveDependencyStopTransitionsItToFailedAndDependentsToStopped() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        FakeUnit unit2 = new FakeUnit("unit2", bus);
        FakeUnit unit3 = new FakeUnit("unit3", bus);
        unit1.addDependency("unit2");
        unit2.addDependency("unit3");
        bus.sink().accept(command("unit1", START));
        unit3.completeStart();
        unit2.completeStart();
        unit1.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET).withDependencies("unit2"),
                unit("unit2").withState(STARTED).withDesiredState(UNSET).withDependencies("unit3"),
                unit("unit3").withState(STARTED).withDesiredState(UNSET)
        ).build());

        // WHEN
        bus.sink().accept(command("unit3", STOP));
        unit3.failStop();
        unit2.completeStop();
        unit1.completeStop();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(UNSET).withDependencies("unit2"),
                unit("unit2").withState(STOPPED).withDesiredState(UNSET).withDependencies("unit3"),
                unit("unit3").withState(FAILED).withDesiredState(UNSET)
        ).build());
    }

}
