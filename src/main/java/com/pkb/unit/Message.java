package com.pkb.unit;

import org.immutables.value.Value;

@Value.Immutable
public interface Message<T> {
    Class<T> messageType();
}
