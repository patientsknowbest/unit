package com.pkb.unit;

import static com.pkb.unit.Filters.payloads;
import static com.pkb.unit.Unchecked.unchecked;
import static com.pkb.unit.message.ImmutableMessage.message;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.pkb.unit.message.payload.Dependencies;
import com.pkb.unit.message.payload.NewUnit;
import com.pkb.unit.message.payload.ReportDependenciesRequest;
import com.pkb.unit.message.payload.ReportStateRequest;
import com.pkb.unit.message.payload.Transition;

import io.reactivex.disposables.Disposable;

/**
 * Tracker
 * keeps track of the state of all units, for reporting purposes.
 */
public class Tracker {
    private Disposable newUnitsSubscription;
    private Disposable transitionsSubscription;
    private Disposable dependenciesSubscription;

    // Parallel model of the world
    public static class Unit {
        String id;
        List<String> dependencies;
        State state;

        public Unit(String id) {
            this.id = id;
            this.dependencies = emptyList();
            state = null;
        }

        public String getId() {
            return id;
        }

        public List<String> getDependencies() {
            return dependencies;
        }

        public void setDependencies(List<String> dependencies) {
            this.dependencies = unmodifiableList(dependencies);
        }

        public State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
        }
    }
    private Map<String, Unit> units = new ConcurrentHashMap<>();

    public Tracker(Bus bus) {
        transitionsSubscription = payloads(bus.events(), Transition.class)
                .subscribe(this::handleTransition);
        dependenciesSubscription = payloads(bus.events(), Dependencies.class)
                .subscribe(this::handleDependencies);
        newUnitsSubscription = payloads(bus.events(), NewUnit.class)
                .subscribe(this::handleNewUnit);

        // Trigger at least one report of dependencies from each unit
        unchecked(() -> bus.sink().accept(message(ReportStateRequest.class)));
        unchecked(() -> bus.sink().accept(message(ReportDependenciesRequest.class)));
    }

    private void handleNewUnit(NewUnit newUnit) {
        units.put(newUnit.id(), new Unit(newUnit.id()));
    }

    private void handleDependencies(Dependencies dependencies) {
        units.compute(dependencies.unitId(), (id, unit) -> {
            if (unit == null) {
                unit = new Unit(dependencies.unitId());
            }
            unit.setDependencies(dependencies.dependencies());
            return unit;
        });
    }

    private void handleTransition(Transition transition) {
        units.compute(transition.unitId(), (id, unit) -> {
            if (unit == null) {
                unit = new Unit(transition.unitId());
            }
            unit.setState(transition.current());
            return unit;
        });
    }

    public Map<String, Unit> getUnits() {
        return units;
    }
}
