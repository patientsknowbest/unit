package com.pkb.unit.tracker;

import java.util.List;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(builder = "systemState")
public interface SystemState {
    List<Unit> units();
}