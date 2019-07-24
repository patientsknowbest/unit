package com.pkb.unit.message.payload;

import java.io.Serializable;
import java.util.List;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

/**
 * Holds the dependencies of a certain unit.
 */
@Immutable
@Style(allParameters = true, of = "dependencies")
public interface Dependencies extends Serializable {

    /**
     * @return the identifier of the unit for the listed dependencies
     */
    String unitId();

    /**
     * @return the list of dependencies of the unit with the given {@code unitId}
     */
    List<String> dependencies();
}
