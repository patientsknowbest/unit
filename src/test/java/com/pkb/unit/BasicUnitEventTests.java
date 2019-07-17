package com.pkb.unit;

import static com.pkb.unit.DesiredState.UNSET;
import static com.pkb.unit.State.CREATED;
import static com.pkb.unit.tracker.ImmutableSystemState.systemState;
import static com.pkb.unit.tracker.ImmutableUnit.unit;

import org.junit.Test;

public class BasicUnitEventTests extends AbstractUnitTest {
    @Test
    public void initialStateIsCreated() {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        new FakeUnit("unit1", bus);
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(CREATED).withDesiredState(UNSET)).build());
    }
}
