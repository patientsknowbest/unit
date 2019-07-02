package com.pkb.unit.message.payload;

import java.io.Serializable;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(of = "newUnit")
public interface NewUnit extends Serializable {
    @Value.Parameter String id();
}
