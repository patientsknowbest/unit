package com.pkb.unit.tracker;

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.pkb.unit.DesiredState;
import com.pkb.unit.State;

@Value.Immutable
@Value.Style(of = "unit")
public interface Unit {
    @Value.Parameter
    String id();
    List<String> dependencies();
    Optional<State> state();
    Optional<DesiredState> desiredState();
}
