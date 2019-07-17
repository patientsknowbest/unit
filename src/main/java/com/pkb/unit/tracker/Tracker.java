package com.pkb.unit.tracker;

import static com.pkb.unit.Unchecked.unchecked;
import static com.pkb.unit.message.ImmutableMessage.message;
import static com.pkb.unit.tracker.ImmutableSystemState.systemState;
import static com.pkb.unit.tracker.ImmutableUnit.unit;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.TreeMap;

import com.pkb.unit.Bus;
import com.pkb.unit.message.payload.Dependencies;
import com.pkb.unit.message.payload.NewUnit;
import com.pkb.unit.message.payload.ReportDependenciesRequest;
import com.pkb.unit.message.payload.ReportStateRequest;
import com.pkb.unit.message.payload.Transition;

import io.reactivex.Observable;

/**
 * Tracker
 * An Observable model of the system state, used for building dashboards and in tests.
 */
public class Tracker {
    /**
     * @param bus The bus containing units to be tracked
     * @return An observable which emits the current system state.
     */
    public static Observable<SystemState> track(Bus bus) {
        return bus.events().filter(msg -> msg.payload().isPresent())
                .map(msg -> msg.payload().get())
                // Build a model of the system state applying changes as they occur.
                .scan(systemState().build(), Tracker::accumulate)
                // Trigger at least one report of dependencies from each unit (allows the tracker to be added at
                // any time)
                .doOnSubscribe((disp) -> {
                    unchecked(() -> bus.sink().accept(message(ReportStateRequest.class)));
                    unchecked(() -> bus.sink().accept(message(ReportDependenciesRequest.class)));
                });
    }

    private static SystemState accumulate(SystemState initial, Object payload) {
        Map<String, Unit> newUnits = initial.units().stream().collect(toMap(Unit::id, identity(), (a, b) -> a, TreeMap::new));

        if (payload instanceof Transition) {
            Transition transition = (Transition) payload;
            newUnits.compute(transition.unitId(), (id, unit) -> {
                ImmutableUnit.Builder bld = ImmutableUnit.builder();
                if (unit == null) {
                    bld.id(transition.unitId());
                } else {
                    bld.from(unit);
                }
                return bld.state(transition.current())
                        .desiredState(transition.currentDesired())
                        .build();
            });
        } else if (payload instanceof Dependencies) {
            Dependencies dependencies = (Dependencies) payload;
            newUnits.compute(dependencies.unitId(), (id, unit) -> {
                ImmutableUnit.Builder bld = ImmutableUnit.builder();
                if (unit == null) {
                    bld.id(dependencies.unitId());
                } else {
                    bld.from(unit);
                }
                return bld.dependencies(dependencies.dependencies()).build();
            });
        } else if (payload instanceof NewUnit) {
            NewUnit newUnit = (NewUnit) payload;
            String id = newUnit.id();
            newUnits.put(id, unit(id));
        }

        return ImmutableSystemState.copyOf(initial).withUnits(newUnits.values());
    }
}
