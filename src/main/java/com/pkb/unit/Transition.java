package com.pkb.unit;

import java.io.Serializable;
import java.util.Optional;

import org.immutables.value.Value;

/**
 * Transition represents a state change.
 */
@Value.Immutable
public interface Transition extends Serializable {
    State current();
    State previous();
    String id();
    Optional<String> comment();
}
