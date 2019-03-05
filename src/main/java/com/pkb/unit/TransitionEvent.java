package com.pkb.unit;

import org.immutables.value.Value;

@Value.Immutable
public interface TransitionEvent extends Event<Transition> {
}
