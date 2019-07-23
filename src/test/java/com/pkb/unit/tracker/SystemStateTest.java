package com.pkb.unit.tracker;

import static com.github.karsaig.approvalcrest.matcher.Matchers.sameContentAsApproved;
import static com.pkb.unit.DesiredState.ENABLED;
import static com.pkb.unit.State.STARTED;
import static com.pkb.unit.State.STARTING;
import static com.pkb.unit.dot.DOT.toDOTFormat;
import static com.pkb.unit.tracker.ImmutableSystemState.systemState;
import static com.pkb.unit.tracker.ImmutableUnit.unit;
import static org.junit.Assert.assertThat;

import org.junit.Test;

// 8828bd
public class SystemStateTest {

    // 5c0b98
    @Test
    public void unitsAreSorted() {
        // GIVEN
        SystemState systemState = systemState().addUnits(
                unit("B").withDesiredState(ENABLED).withState(STARTING).withDependencies("A"),
                unit("A").withDesiredState(ENABLED).withState(STARTING).withDependencies("C"),
                unit("C").withDesiredState(ENABLED).withState(STARTED)
        ).build();
        // WHEN
        String dotFormat = toDOTFormat(systemState);
        // THEN
        assertThat(dotFormat, sameContentAsApproved());
    }
}