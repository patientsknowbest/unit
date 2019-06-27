package com.pkb.unit;

import java.io.Serializable;
import java.util.Map;

import org.immutables.value.Value;

@Value.Immutable
public interface Dependencies extends Serializable {
    String id();
    Map<String, State> dependencies();
}
