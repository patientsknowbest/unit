package com.pkb.unit;

import static com.pkb.unit.DesiredState.UNSET;
import static com.pkb.unit.State.CREATED;
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
                unit(unit1ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unit2ID),
                unit(unit2ID).withState(CREATED).withDesiredState(UNSET)
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
                unit(unit1ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unit2ID, unit3ID, unit4ID),
                unit(unit2ID).withState(CREATED).withDesiredState(UNSET),
                unit(unit3ID).withState(CREATED).withDesiredState(UNSET),
                unit(unit4ID).withState(CREATED).withDesiredState(UNSET)
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
                unit(unit1ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unit2ID),
                unit(unit2ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unit3ID),
                unit(unit3ID).withState(CREATED).withDesiredState(UNSET)
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
                unit(unit1ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unit2ID),
                unit(unit2ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unit3ID, unit4ID, unit5ID),
                unit(unit3ID).withState(CREATED).withDesiredState(UNSET),
                unit(unit4ID).withState(CREATED).withDesiredState(UNSET),
                unit(unit5ID).withState(CREATED).withDesiredState(UNSET)
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
                unit(unit1ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unit2ID),
                unit(unit2ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unit3ID),
                unit(unit3ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unit4ID),
                unit(unit4ID).withState(CREATED).withDesiredState(UNSET)
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
                unit(unit1ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unit2ID),
                unit(unit2ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unit3ID),
                unit(unit3ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unit4ID, unit5ID),
                unit(unit4ID).withState(CREATED).withDesiredState(UNSET),
                unit(unit5ID).withState(CREATED).withDesiredState(UNSET)
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
                unit(unitA1ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unitA2ID, unitA3ID),
                unit(unitA2ID).withState(CREATED).withDesiredState(UNSET),
                unit(unitA3ID).withState(CREATED).withDesiredState(UNSET),
                unit(unitB1ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unitB2ID, unitB3ID),
                unit(unitB2ID).withState(CREATED).withDesiredState(UNSET),
                unit(unitB3ID).withState(CREATED).withDesiredState(UNSET)
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
                unit(unit1ID).withState(CREATED).withDesiredState(UNSET),
                unit(unit2ID).withState(CREATED).withDesiredState(UNSET)
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
                unit(unit1ID).withState(CREATED).withDesiredState(UNSET),
                unit(unit2ID).withState(CREATED).withDesiredState(UNSET),
                unit(unit3ID).withState(CREATED).withDesiredState(UNSET),
                unit(unit4ID).withState(CREATED).withDesiredState(UNSET)
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
                unit(unit1ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unit2ID),
                unit(unit2ID).withState(CREATED).withDesiredState(UNSET),
                unit(unit3ID).withState(CREATED).withDesiredState(UNSET)
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
                unit(unit1ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unit2ID),
                unit(unit2ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unit3ID),
                unit(unit3ID).withState(CREATED).withDesiredState(UNSET),
                unit(unit4ID).withState(CREATED).withDesiredState(UNSET),
                unit(unit5ID).withState(CREATED).withDesiredState(UNSET)
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
                unit(unit1ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unit2ID),
                unit(unit2ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unit3ID),
                unit(unit3ID).withState(CREATED).withDesiredState(UNSET),
                unit(unit4ID).withState(CREATED).withDesiredState(UNSET)
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
                unit(unit1ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unit2ID),
                unit(unit2ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unit3ID),
                unit(unit3ID).withState(CREATED).withDesiredState(UNSET),
                unit(unit4ID).withState(CREATED).withDesiredState(UNSET),
                unit(unit5ID).withState(CREATED).withDesiredState(UNSET)
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
                unit(unitA1ID).withState(CREATED).withDesiredState(UNSET),
                unit(unitA2ID).withState(CREATED).withDesiredState(UNSET),
                unit(unitA3ID).withState(CREATED).withDesiredState(UNSET),
                unit(unitB1ID).withState(CREATED).withDesiredState(UNSET).withDependencies(unitB3ID),
                unit(unitB2ID).withState(CREATED).withDesiredState(UNSET),
                unit(unitB3ID).withState(CREATED).withDesiredState(UNSET)
        ).build());
    }

}
