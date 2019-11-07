package com.pkb.unit.tracker;

import static com.pkb.unit.Filters.payloads;
import static com.pkb.unit.State.STARTED;
import static com.pkb.unit.State.STOPPED;
import static com.pkb.unit.Unchecked.unchecked;
import static com.pkb.unit.message.ImmutableMessage.message;
import static com.pkb.unit.tracker.ImmutableSystemState.systemState;
import static com.pkb.unit.tracker.ImmutableUnit.unit;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import com.pkb.unit.Bus;
import com.pkb.unit.message.payload.Dependencies;
import com.pkb.unit.message.payload.NewUnit;
import com.pkb.unit.message.payload.ReportDependenciesRequest;
import com.pkb.unit.message.payload.ReportStateRequest;
import com.pkb.unit.message.payload.Transition;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Tracker
 * An Observable model of the system state, used for building dashboards and in tests.
 */
public class Tracker {
    private Tracker() {}

    private static class RestartTracker implements ObservableSource<Boolean> {
        private boolean hasStopped;
        private boolean hasStarted;

        private Disposable disposable;

        private Observer<? super Boolean> observer;

        RestartTracker(Bus bus, String id) {
            hasStopped = false;
            hasStarted = false;
            disposable = payloads(bus.events(), Transition.class)
                    .filter(transition -> Objects.equals(transition.unitId(), id))
                    .subscribe(this::onTransition);
        }

        private void onTransition(Transition x) {
            if (!hasStopped && x.current() == STOPPED) {
                hasStopped = true;
            } else if (hasStopped && x.current() == STARTED) {
                hasStarted = true;
                disposable.dispose();
                observer.onNext(true);
                observer.onComplete();
            }
        }

        @Override
        public void subscribe(Observer<? super Boolean> observer) {
            this.observer = observer;
            observer.onSubscribe(disposable);
        }
    }

    /**
     * Creates an observable that emits 'true' when a given unit has transitioned from
     * started -> stopped -> started. This can be useful to ensure that a restart
     * has taken place when desired, and that the unit has returned to an available state.
     * @param bus the bus to observe for events
     * @param id the ID of the unit to be observed for a restart
     * @return An observable, which emits true & completes when the desired unit has restarted.
     */
    public static Observable<Boolean> unitRestarted(Bus bus, String id) {
        return Observable.wrap(new RestartTracker(bus, id));
    }

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
                .doOnSubscribe(ignored -> {
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
