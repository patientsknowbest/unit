package com.pkb.unit;

import static com.pkb.unit.DesiredState.UNSET;
import static com.pkb.unit.State.FAILED;
import static com.pkb.unit.State.STOPPED;
import static com.pkb.unit.tracker.ImmutableSystemState.systemState;
import static com.pkb.unit.tracker.ImmutableUnit.unit;

import org.junit.Test;

public class BasicUnitEventTests extends AbstractUnitTest {
    @Test
    public void initialStateIsStopped() {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        new FakeUnit("unit1", bus);
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withState(STOPPED).withDesiredState(UNSET))
                .build());
    }

    @Test
    public void whenAdvertiseFailedOnStoppedThenFailed() throws Exception {
        // GIVEN
        setupComputationAndIOTestScheduler();

        // WHEN
        FakeUnit fakeUnit = new FakeUnit("unit1", bus);
        fakeUnit.failed();
        testScheduler.triggerActions();

        // THEN
        assertLatestState(systemState().addUnits(
                unit("unit1").withDesiredState(UNSET).withState(FAILED))
                .build());
    }
}
