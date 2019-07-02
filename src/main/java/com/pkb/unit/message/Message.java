package com.pkb.unit.message;

import java.util.Optional;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;
import org.immutables.value.Value.Style;

@Immutable
@Style(of = "message")
public interface Message<T> {
    @Parameter
    Class<T> messageType();
    Optional<T> payload();
    Optional<String> target();
}
