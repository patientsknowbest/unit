package com.pkb.unit.tracker;

import static com.github.karsaig.approvalcrest.matcher.Matchers.sameContentAsApproved;
import static com.pkb.unit.DesiredState.ENABLED;
import static com.pkb.unit.State.STARTING;
import static com.pkb.unit.dot.DOT.toDOTFormat;
import static com.pkb.unit.tracker.ImmutableSystemState.systemState;
import static com.pkb.unit.tracker.ImmutableUnit.unit;
import static org.junit.Assert.assertThat;

import org.junit.Test;

// 54c6da
public class UnitTest {
    // b9ba9d
    @Test
    public void dependenciesAreOrdered() {
        // GIVEN
        SystemState systemState = systemState().addUnits(
                unit("1").withDesiredState(ENABLED).withState(STARTING).withDependencies("B", "A", "C")
        ).build();
        // WHEN
        String dotFormat = toDOTFormat(systemState);
        // THEN
        assertThat(dotFormat, sameContentAsApproved());
    }
}