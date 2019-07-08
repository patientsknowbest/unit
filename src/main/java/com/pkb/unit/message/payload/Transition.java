package com.pkb.unit.message.payload;

import java.io.Serializable;
import java.util.Optional;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

import com.pkb.unit.State;

/**
 * Transition represents a state change.
 */
@Immutable
@Style(of = "transition", allParameters = true)
public interface Transition extends Serializable {
    State current();
    State previous();
    String unitId();
    Optional<String> comment();
}
