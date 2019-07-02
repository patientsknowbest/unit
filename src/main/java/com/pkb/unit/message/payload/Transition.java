package com.pkb.unit.message.payload;

import java.io.Serializable;
import java.util.Optional;

import org.immutables.value.Value;

import com.pkb.unit.State;

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
