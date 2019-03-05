package com.pkb.unit;

import io.reactivex.annotations.Nullable;
import org.immutables.value.Value;

@Value.Immutable
public interface Transition {
    State current();
    State previous();
    String id();
    @Nullable String comment();
}
