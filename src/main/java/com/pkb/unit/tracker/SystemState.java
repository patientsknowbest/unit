package com.pkb.unit.tracker;

import static com.pkb.unit.tracker.ImmutableSystemState.systemState;

import java.util.Comparator;
import java.util.List;

import org.immutables.value.Value;

import com.google.common.collect.Comparators;

/**
 * Keeps track of the whole system, the units and their states.
 */
@Value.Immutable
@Value.Style(builder = "systemState")
public interface SystemState {

    /**
     * @return all the units that exist in the system
     */
    List<Unit> units();

    /**
     * @return the ordered version of SystemState
     */
    @Value.Check
    default SystemState normalize() {
        io.vavr.collection.List<Unit> units = io.vavr.collection.List.ofAll(units());
        Comparator<String> comparator = Comparator.naturalOrder();
        if (Comparators.isInOrder(units.map(Unit::id), comparator)) {
            return this;
        } else {
            return systemState().from(this)
                    .units(units.sortBy(comparator, Unit::id))
                    .build();
        }
    }
}