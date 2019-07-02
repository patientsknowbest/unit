package com.pkb.unit.message;

import org.immutables.value.Value;

import com.pkb.unit.message.Message;

@Value.Immutable
public interface UnicastMessage<T> extends Message<T> {
    String target();
}
