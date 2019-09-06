package com.pkb.unit.tracker;

import static com.pkb.unit.Unchecked.unchecked;
import static com.pkb.unit.tracker.ImmutableSystemState.systemState;
import static com.pkb.unit.tracker.ImmutableUnit.unit;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import javax.annotation.Nullable;

import com.pkb.unit.Bus;
import com.pkb.unit.Dependencies;
import com.pkb.unit.Message;
import com.pkb.unit.NewUnit;
import com.pkb.unit.ReportDependenciesRequest;
import com.pkb.unit.ReportStateRequest;
import com.pkb.unit.Transition;

import io.reactivex.Observable;
import io.vavr.collection.List;

/**
 * Tracker
 * An Observable model of the system state, used for building dashboards and in tests.
 */
public class Tracker {
    private Tracker() {}

    /**
     * @param bus The bus containing units to be tracked
     * @return An observable which emits the current system state.
     */
    public static Observable<SystemState> track(Bus bus) {
        return bus.events().map(Message::getPayload)
                .filter(Objects::nonNull)
                // Build a model of the system state applying changes as they occur.
                .scan(systemState().build(), Tracker::accumulate)
                // Trigger at least one report of dependencies from each unit (allows the tracker to be added at
                // any time)
                .doOnSubscribe(ignored -> {
                    unchecked(() -> bus.sink().accept(message(null, ReportStateRequest.newBuilder().build())));
                    unchecked(() -> bus.sink().accept(message(null, ReportDependenciesRequest.newBuilder().build())));
                });
    }

    private static SystemState accumulate(SystemState initial, Object getPayload) {
        Map<String, Unit> newUnits = initial.units().stream().collect(toMap(Unit::id, identity(), (a, b) -> a, TreeMap::new));

        if (getPayload instanceof Transition) {
            Transition transition = (Transition) getPayload;
            newUnits.compute(transition.getUnitId().toString(), (id, unit) -> {
                ImmutableUnit.Builder bld = ImmutableUnit.builder();
                if (unit == null) {
                    bld.id(transition.getUnitId().toString());
                } else {
                    bld.from(unit);
                }
                return bld.state(transition.getCurrent())
                        .desiredState(transition.getCurrentDesired())
                        .build();
            });
        } else if (getPayload instanceof Dependencies) {
            Dependencies dependencies = (Dependencies) getPayload;
            newUnits.compute(dependencies.getUnitId().toString(), (id, unit) -> {
                ImmutableUnit.Builder bld = ImmutableUnit.builder();
                if (unit == null) {
                    bld.id(dependencies.getUnitId().toString());
                } else {
                    bld.from(unit);
                }
                return bld.dependencies(List.ofAll(dependencies.getDependencies()).map(String::valueOf)).build();
            });
        } else if (getPayload instanceof NewUnit) {
            NewUnit newUnit = (NewUnit) getPayload;
            String id = newUnit.getId().toString();
            newUnits.put(id, unit(id));
        }

        return ImmutableSystemState.copyOf(initial).withUnits(newUnits.values());
    }

    private static <T> Message message(@Nullable CharSequence target, T payload) {
        return Message.newBuilder()
                .setTarget(target)
                .setPayload(payload)
                .build();
    }
}
