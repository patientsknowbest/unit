package com.pkb.unit;

import java.io.Serializable;

import org.immutables.value.Value;

@Value.Immutable
public interface NewUnit extends Serializable {
    String id();
}
