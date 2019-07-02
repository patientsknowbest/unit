package com.pkb.unit.message;

import org.immutables.value.Value;

import java.io.Serializable;

import com.pkb.unit.message.MessageWithPayload;
import com.pkb.unit.message.UnicastMessage;

@Value.Immutable
public interface UnicastMessageWithPayload<T extends Serializable> extends UnicastMessage<T>, MessageWithPayload<T> {
}
