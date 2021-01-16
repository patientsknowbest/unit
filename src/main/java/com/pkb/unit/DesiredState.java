package com.pkb.unit;

/**
 * DesiredState represents some external configuration of the system.
 *
 * Users of the system should define which units they want to be in a particular state
 * and the system will endeavour to meet the desired state, e.g. by starting or stopping.
 */
public enum DesiredState {
    /**
     * Enabled units will always try to be 'started'. They will initially move from created -&gt; starting,
     * and if they fail then they will move from failed -&gt; starting after some cool off period.
     */
    ENABLED,

    /**
     * Disabled units cannot be started. If a started unit is disabled, then it will stop.
     */
    DISABLED,

    /**
     * Desired state need not be defined for every unit. If a unit is a dependency only and not directly used by client code,
     * it doesn't try to meet any particular state on it's own.
     */
    UNSET
}
