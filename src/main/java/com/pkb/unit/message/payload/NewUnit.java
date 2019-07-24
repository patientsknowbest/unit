package com.pkb.unit.message.payload;

import java.io.Serializable;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

/**
 * Message type that signals that a new unit has been created.
 */
@Immutable
@Style(allParameters = true, of = "newUnit")
public interface NewUnit extends Serializable {
    /**
     * @return the identifier of the newly created unit
     */
    String id();
}
