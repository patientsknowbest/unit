package com.pkb.unit.message.payload;

import java.io.Serializable;
import java.util.Optional;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

import com.pkb.unit.DesiredState;
import com.pkb.unit.State;

/**
 * Transition represents a state change.
 */
@Immutable
@Style(of = "transition", allParameters = true)
public interface Transition extends Serializable {
    /**
     * @return the idintifier of the unit that transitioned
     */
    String unitId();

    /**
     * @return the state of the unit it is now in
     */
    State current();

    /**
     * @return the state of the unit it was in before this transition
     */
    State previous();

    /**
     * @return the desired state of the unit it is now in
     */
    DesiredState currentDesired();

    /**
     * @return the desired state of the unit it was in before this transition
     */
    DesiredState previousDesired();

    /**
     * @return additional information about the transition
     */
    Optional<String> comment();
}
