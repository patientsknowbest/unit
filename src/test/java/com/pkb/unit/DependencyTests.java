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

public class DependencyTests extends AbstractUnitTest {

    @Test
    public void testAddOneDependency() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        String unit1ID = "unit1";
        String unit2ID = "unit2";

        // WHEN
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        unit1.addDependency(unit2ID);
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit(unit1ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unit2ID),
                unit(unit2ID).withState(STOPPED).withDesiredState(UNSET)
        ).build());
    }

    @Test
    public void testAddMoreDependencies() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        String unit4ID = "unit4";

        // WHEN
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        FakeUnit unit4 = new FakeUnit(unit4ID, bus);
        unit1.addDependency(unit2ID);
        unit1.addDependency(unit3ID);
        unit1.addDependency(unit4ID);
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit(unit1ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unit2ID, unit3ID, unit4ID),
                unit(unit2ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unit3ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unit4ID).withState(STOPPED).withDesiredState(UNSET)
        ).build());
    }

    @Test
    public void testAddOneTransitiveDependency() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";

        // WHEN
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        unit1.addDependency(unit2ID);
        unit2.addDependency(unit3ID);
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit(unit1ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unit2ID),
                unit(unit2ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unit3ID),
                unit(unit3ID).withState(STOPPED).withDesiredState(UNSET)
        ).build());
    }

    @Test
    public void testAddMoreTransitiveDependencies() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        String unit4ID = "unit4";
        String unit5ID = "unit5";

        // WHEN
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        FakeUnit unit4 = new FakeUnit(unit4ID, bus);
        FakeUnit unit5 = new FakeUnit(unit5ID, bus);
        unit1.addDependency(unit2ID);
        unit2.addDependency(unit3ID);
        unit2.addDependency(unit4ID);
        unit2.addDependency(unit5ID);
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit(unit1ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unit2ID),
                unit(unit2ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unit3ID, unit4ID, unit5ID),
                unit(unit3ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unit4ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unit5ID).withState(STOPPED).withDesiredState(UNSET)
        ).build());
    }

    @Test
    public void testAddTwoDepthTransitiveDependency() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        String unit4ID = "unit4";

        // WHEN
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        FakeUnit unit4 = new FakeUnit(unit4ID, bus);
        unit1.addDependency(unit2ID);
        unit2.addDependency(unit3ID);
        unit3.addDependency(unit4ID);
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit(unit1ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unit2ID),
                unit(unit2ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unit3ID),
                unit(unit3ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unit4ID),
                unit(unit4ID).withState(STOPPED).withDesiredState(UNSET)
        ).build());
    }

    @Test
    public void testAddMoreTwoDepthTransitiveDependencies() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        String unit4ID = "unit4";
        String unit5ID = "unit5";

        // WHEN
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        FakeUnit unit4 = new FakeUnit(unit4ID, bus);
        FakeUnit unit5 = new FakeUnit(unit5ID, bus);
        unit1.addDependency(unit2ID);
        unit2.addDependency(unit3ID);
        unit3.addDependency(unit4ID);
        unit3.addDependency(unit5ID);
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit(unit1ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unit2ID),
                unit(unit2ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unit3ID),
                unit(unit3ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unit4ID, unit5ID),
                unit(unit4ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unit5ID).withState(STOPPED).withDesiredState(UNSET)
        ).build());
    }

    @Test
    public void testAddDependenciesToDifferentDependencyTrees() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        String unitA1ID = "unitA1";
        String unitA2ID = "unitA2";
        String unitA3ID = "unitA3";
        String unitB1ID = "unitB1";
        String unitB2ID = "unitB2";
        String unitB3ID = "unitB3";

        // WHEN
        FakeUnit unit1a = new FakeUnit(unitA1ID, bus);
        FakeUnit unit2a = new FakeUnit(unitA2ID, bus);
        FakeUnit unit3a = new FakeUnit(unitA3ID, bus);
        FakeUnit unit1b = new FakeUnit(unitB1ID, bus);
        FakeUnit unit2b = new FakeUnit(unitB2ID, bus);
        FakeUnit unit3b = new FakeUnit(unitB3ID, bus);
        unit1a.addDependency(unitA2ID);
        unit1a.addDependency(unitA3ID);
        unit1b.addDependency(unitB2ID);
        unit1b.addDependency(unitB3ID);
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit(unitA1ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unitA2ID, unitA3ID),
                unit(unitA2ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unitA3ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unitB1ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unitB2ID, unitB3ID),
                unit(unitB2ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unitB3ID).withState(STOPPED).withDesiredState(UNSET)
        ).build());
    }

    @Test
    public void testRemoveOneDependency() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        String unit1ID = "unit1";
        String unit2ID = "unit2";

        // WHEN
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        unit1.addDependency(unit2ID);
        unit1.removeDependency(unit2ID);
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit(unit1ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unit2ID).withState(STOPPED).withDesiredState(UNSET)
        ).build());
    }

    @Test
    public void testRemoveMoreDependencies() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        String unit4ID = "unit4";

        // WHEN
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        FakeUnit unit4 = new FakeUnit(unit4ID, bus);
        unit1.addDependency(unit2ID);
        unit1.addDependency(unit3ID);
        unit1.addDependency(unit4ID);
        unit1.removeDependency(unit2ID);
        unit1.removeDependency(unit3ID);
        unit1.removeDependency(unit4ID);
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit(unit1ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unit2ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unit3ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unit4ID).withState(STOPPED).withDesiredState(UNSET)
        ).build());
    }

    @Test
    public void testRemoveOneTransitiveDependency() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";

        // WHEN
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        unit1.addDependency(unit2ID);
        unit2.addDependency(unit3ID);
        unit2.removeDependency(unit3ID);
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit(unit1ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unit2ID),
                unit(unit2ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unit3ID).withState(STOPPED).withDesiredState(UNSET)
        ).build());
    }

    @Test
    public void testRemoveMoreTransitiveDependencies() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        String unit4ID = "unit4";
        String unit5ID = "unit5";

        // WHEN
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        FakeUnit unit4 = new FakeUnit(unit4ID, bus);
        FakeUnit unit5 = new FakeUnit(unit5ID, bus);
        unit1.addDependency(unit2ID);
        unit2.addDependency(unit3ID);
        unit2.addDependency(unit4ID);
        unit2.addDependency(unit5ID);
        unit2.removeDependency(unit5ID);
        unit2.removeDependency(unit4ID);
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit(unit1ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unit2ID),
                unit(unit2ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unit3ID),
                unit(unit3ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unit4ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unit5ID).withState(STOPPED).withDesiredState(UNSET)
        ).build());
    }

    @Test
    public void testRemoveTwoDepthTransitiveDependency() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        String unit4ID = "unit4";

        // WHEN
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        FakeUnit unit4 = new FakeUnit(unit4ID, bus);
        unit1.addDependency(unit2ID);
        unit2.addDependency(unit3ID);
        unit3.addDependency(unit4ID);
        unit3.removeDependency(unit4ID);
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit(unit1ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unit2ID),
                unit(unit2ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unit3ID),
                unit(unit3ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unit4ID).withState(STOPPED).withDesiredState(UNSET)
        ).build());
    }

    @Test
    public void testRemoveMoreTwoDepthTransitiveDependencies() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        String unit1ID = "unit1";
        String unit2ID = "unit2";
        String unit3ID = "unit3";
        String unit4ID = "unit4";
        String unit5ID = "unit5";

        // WHEN
        FakeUnit unit1 = new FakeUnit(unit1ID, bus);
        FakeUnit unit2 = new FakeUnit(unit2ID, bus);
        FakeUnit unit3 = new FakeUnit(unit3ID, bus);
        FakeUnit unit4 = new FakeUnit(unit4ID, bus);
        FakeUnit unit5 = new FakeUnit(unit5ID, bus);
        unit1.addDependency(unit2ID);
        unit2.addDependency(unit3ID);
        unit3.addDependency(unit4ID);
        unit3.addDependency(unit5ID);
        unit3.removeDependency(unit4ID);
        unit3.removeDependency(unit5ID);
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit(unit1ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unit2ID),
                unit(unit2ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unit3ID),
                unit(unit3ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unit4ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unit5ID).withState(STOPPED).withDesiredState(UNSET)
        ).build());
    }

    @Test
    public void testRemoveDependenciesFromDifferentDependencyTrees() throws Exception {
        // GIVEN
        setupComputationTestScheduler();
        String unitA1ID = "unitA1";
        String unitA2ID = "unitA2";
        String unitA3ID = "unitA3";
        String unitB1ID = "unitB1";
        String unitB2ID = "unitB2";
        String unitB3ID = "unitB3";

        // WHEN
        FakeUnit unit1a = new FakeUnit(unitA1ID, bus);
        FakeUnit unit2a = new FakeUnit(unitA2ID, bus);
        FakeUnit unit3a = new FakeUnit(unitA3ID, bus);
        FakeUnit unit1b = new FakeUnit(unitB1ID, bus);
        FakeUnit unit2b = new FakeUnit(unitB2ID, bus);
        FakeUnit unit3b = new FakeUnit(unitB3ID, bus);
        unit1a.addDependency(unitA2ID);
        unit1a.addDependency(unitA3ID);
        unit1b.addDependency(unitB2ID);
        unit1b.addDependency(unitB3ID);
        unit1a.removeDependency(unitA2ID);
        unit1a.removeDependency(unitA3ID);
        unit1b.removeDependency(unitB2ID);
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit(unitA1ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unitA2ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unitA3ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unitB1ID).withState(STOPPED).withDesiredState(UNSET).withDependencies(unitB3ID),
                unit(unitB2ID).withState(STOPPED).withDesiredState(UNSET),
                unit(unitB3ID).withState(STOPPED).withDesiredState(UNSET)
        ).build());
    }

    @Test
    public void enableCommandTransitionsUnitAndDependencyToStarting() throws Exception {
        // GIVEN
        setupComputationTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        FakeUnit unit2 = new FakeUnit("unit2", bus);
        unit1.addDependency("unit2");
        unit1.enable();
        testComputationScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTING).withDesiredState(ENABLED).withDependencies("unit2"),
                unit("unit2").withState(STARTING).withDesiredState(UNSET)).build());
    }

    @Test
    public void completeStartTransitionsUnitsToStarted() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        FakeUnit unit2 = new FakeUnit("unit2", bus);
        unit1.addDependency("unit2");
        unit1.enable();
        unit2.completeStart();
        unit1.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED).withDependencies("unit2"),
                unit("unit2").withState(STARTED).withDesiredState(UNSET)).build());
    }

    @Test
    public void whenDependencyAlreadyStoppedThenStopDoesNothing() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        FakeUnit unit2 = new FakeUnit("unit2", bus);
        unit1.addDependency("unit2");
        unit1.start();
        unit2.completeStart();
        unit1.completeStart();

        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(UNSET).withState(STARTED).withDependencies("unit2"),
                unit("unit2").withDesiredState(UNSET).withState(STARTED)).build());

        // WHEN
        unit2.stop();
        unit2.completeStop();
        unit1.completeStop();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(UNSET).withState(STOPPED).withDependencies("unit2"),
                unit("unit2").withDesiredState(UNSET).withState(STOPPED)).build());

        // WHEN
        unit2.stop();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(UNSET).withState(STOPPED).withDependencies("unit2"),
                unit("unit2").withDesiredState(UNSET).withState(STOPPED)).build());
    }


    @Test
    public void unsuccessfulStartTransitionsIndependentUnitsToFailed() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        FakeUnit unit2 = new FakeUnit("unit2", bus);
        unit1.start();
        unit2.start();
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
        unit1.enable();
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
        unit1.start();
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
        unit1.start();
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
        unit1.start();
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
        unit1.start();
        unit1.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET)
        ).build());

        // WHEN
        unit1.stop();
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
        unit1.start();
        unit2.start();
        unit1.completeStart();
        unit2.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET),
                unit("unit2").withState(STARTED).withDesiredState(UNSET)
        ).build());

        // WHEN
        unit1.stop();
        unit2.stop();
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
        unit1.start();
        unit1.completeStart();
        unit2.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(UNSET).withDependencies("unit2"),
                unit("unit2").withState(STARTED).withDesiredState(UNSET)
        ).build());

        // WHEN
        unit2.stop();
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
        unit1.start();
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
        unit3.stop();
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

    @Test
    public void disabledReenableDependency() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit unit1 = new FakeUnit("unit1", bus);
        FakeUnit unit2 = new FakeUnit("unit2", bus);
        unit1.addDependency("unit2");
        unit1.enable();
        unit1.completeStart();
        unit2.completeStart();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED).withDependencies("unit2"),
                unit("unit2").withState(STARTED).withDesiredState(UNSET)
        ).build());

        // WHEN
        unit2.disable();
        unit2.completeStop();
        unit1.completeStop();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTING).withDesiredState(ENABLED).withDependencies("unit2"),
                unit("unit2").withState(STOPPED).withDesiredState(DISABLED)
        ).build());

        // WHEN
        unit2.clearDesiredState();
        unit1.completeStart();
        unit2.completeStart();
        testScheduler.advanceTimeBy(FakeUnit.RETRY_PERIOD, FakeUnit.RETRY_PERIOD_UNIT);

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STARTED).withDesiredState(ENABLED).withDependencies("unit2"),
                unit("unit2").withState(STARTED).withDesiredState(UNSET)
        ).build());
    }
}
