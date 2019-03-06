package com.pkb.unit;

import org.immutables.value.Value;

@Value.Immutable
public interface UnicastMessage<T> extends Message<T> {
    String target();
}
