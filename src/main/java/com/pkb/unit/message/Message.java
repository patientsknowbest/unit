package com.pkb.unit.message;

import java.util.Optional;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;
import org.immutables.value.Value.Style;

/**
 * An event that can be sent to the {@link Bus} used for communication
 * between units.
 * 
 * @param <T> the type of the message
 */
@Immutable
@Style(of = "message")
public interface Message<T> {
    /**
     * @return the type of the message
     */
    @Parameter
    Class<T> messageType();

    /**
     * @return additional information included in this message
     */
    Optional<T> payload();

    /**
     * @return the intended recepient of this message
     */
    Optional<String> target();
}
