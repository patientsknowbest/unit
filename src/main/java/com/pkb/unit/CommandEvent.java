package com.pkb.unit;

import org.immutables.value.Value;

@Value.Immutable
public interface CommandEvent extends Event<Command> {
    String targetId();
}
