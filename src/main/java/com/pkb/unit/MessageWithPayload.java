package com.pkb.unit;

import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface MessageWithPayload<T extends Serializable> extends Message<T> {
    T payload();
}
