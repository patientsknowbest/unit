package com.pkb.unit;

import static com.pkb.unit.DesiredState.UNSET;
import static com.pkb.unit.State.CREATED;
import static com.pkb.unit.tracker.ImmutableSystemState.systemState;
import static com.pkb.unit.tracker.ImmutableUnit.unit;

import org.junit.Test;

import com.pkb.unit.tracker.ImmutableSystemState;
import com.pkb.unit.tracker.SystemState;

import com.google.common.collect.ImmutableMap;

import io.reactivex.observers.TestObserver;

public class BasicUnitEventTests extends AbstractUnitTest {
    @Test
    public void initialStateIsCreated() {
        // GIVEN
        ImmutableSystemState expected = systemState(
                ImmutableMap.of("unit1", unit("unit1").withState(CREATED).withDesiredState(UNSET))
        );
        TestObserver<SystemState> testObserver = testObserver(expected);

        // WHEN
        new FakeUnit("unit1", bus);

        // THEN
        assertExpectedState(testObserver, expected);
    }
}
