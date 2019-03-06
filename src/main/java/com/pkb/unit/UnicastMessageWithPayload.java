package com.pkb.unit;

import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface UnicastMessageWithPayload<T extends Serializable> extends UnicastMessage<T>, MessageWithPayload<T> {
}
