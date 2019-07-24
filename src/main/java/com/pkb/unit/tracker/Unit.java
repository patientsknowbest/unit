package com.pkb.unit.tracker;

import static java.util.function.Function.identity;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.pkb.unit.DesiredState;
import com.pkb.unit.State;

import com.google.common.collect.Comparators;

/**
 * A non-functional copy of real units for testing and visual representation.
 */
@Value.Immutable
@Value.Style(of = "unit")
public interface Unit {
    @Value.Parameter
    String id();
    List<String> dependencies();
    Optional<State> state();
    Optional<DesiredState> desiredState();

    @Value.Check
    default Unit normalize() {
        io.vavr.collection.List<String> dependencies = io.vavr.collection.List.ofAll(dependencies());
        Comparator<String> comparator = Comparator.naturalOrder();
        if (Comparators.isInOrder(dependencies, comparator)) {
            return this;
        } else {
            return ImmutableUnit.copyOf(this).withDependencies(dependencies.sortBy(comparator, identity()));
        }
    }
}
