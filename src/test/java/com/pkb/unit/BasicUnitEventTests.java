package com.pkb.unit;

import static com.pkb.unit.DesiredState.UNSET;
import static com.pkb.unit.State.CREATED;
import static com.pkb.unit.tracker.ImmutableSystemState.systemState;
import static com.pkb.unit.tracker.ImmutableUnit.unit;
import static com.pkb.unit.tracker.Tracker.track;

import org.junit.Test;

import com.pkb.unit.tracker.SystemState;

import com.google.common.collect.ImmutableMap;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

public class BasicUnitEventTests {
    @Test
    public void initialStateIsCreated() {
        // GIVEN a
        Bus bus = new LocalBus();
        TestObserver<SystemState> testObserver = testObserver(bus, systemState(
                ImmutableMap.of("unit1", unit("unit1").withState(CREATED).withDesiredState(UNSET))
        ));

        // WHEN
        new FakeUnit("unit1", bus);

        // THEN
        testObserver.awaitCount(1);
    }

    private TestObserver<SystemState> testObserver(Bus bus, SystemState expected) {
        Observable<SystemState> track = track(bus);
        return track
                .filter(state -> state.equals(expected))
                .test();
    }

}
