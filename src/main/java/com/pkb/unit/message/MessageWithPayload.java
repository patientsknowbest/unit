package com.pkb.unit.message;

import org.immutables.value.Value;

import java.io.Serializable;

import com.pkb.unit.message.Message;

@Value.Immutable
public interface MessageWithPayload<T extends Serializable> extends Message<T> {
    T payload();
}
