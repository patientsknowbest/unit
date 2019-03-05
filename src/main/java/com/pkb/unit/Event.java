package com.pkb.unit;

import org.immutables.value.Value;

@Value.Immutable
public interface Event<T> {
    T value();
}
