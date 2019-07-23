package com.pkb.unit.dot;

import static com.github.karsaig.approvalcrest.matcher.Matchers.sameContentAsApproved;
import static com.pkb.unit.DesiredState.ENABLED;
import static com.pkb.unit.State.STARTED;
import static com.pkb.unit.State.STARTING;
import static com.pkb.unit.dot.DOT.toDOTFormat;
import static com.pkb.unit.tracker.ImmutableSystemState.systemState;
import static com.pkb.unit.tracker.ImmutableUnit.unit;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.pkb.unit.tracker.SystemState;

// 9e2959
public class DOTTest {
    // 38dad4
    @Test
    public void testToDOTFormat() {
        // GIVEN
        SystemState systemState = systemState().addUnits(
                unit("unit1").withDesiredState(ENABLED).withState(STARTING).withDependencies("unit2"),
                unit("unit2").withDesiredState(ENABLED).withState(STARTED)
        ).build();
        // WHEN
        String dotFormat = toDOTFormat(systemState);
        // THEN
        assertThat(dotFormat, sameContentAsApproved());
    }
}