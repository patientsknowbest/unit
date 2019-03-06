package com.pkb.unit;

import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Optional;

@Value.Immutable
public interface Transition extends Serializable {
    State current();
    State previous();
    String id();
    Optional<String> comment();
}
