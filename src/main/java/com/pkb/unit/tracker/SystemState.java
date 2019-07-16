package com.pkb.unit.tracker;

import java.util.Map;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(allParameters = true, of = "systemState")
public interface SystemState {
    Map<String, Unit> units();
}