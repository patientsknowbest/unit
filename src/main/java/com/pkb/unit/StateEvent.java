package com.pkb.unit;

import org.immutables.value.Value;

@Value.Immutable
public interface StateEvent extends Event<State> {
}
