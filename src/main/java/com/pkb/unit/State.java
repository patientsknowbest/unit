package com.pkb.unit;

/**
 * Possible states a unit can be in.
 */
public enum State {
    /**
     * Failed state indicates that something went wrong
     * with the unit so it is not operational but can be started.
     */
    FAILED,

    /**
     * Stopped state indicates that the unit is not running but
     * can be started.
     */
    STOPPED,

    /**
     * Stopping state indicates that the unit is about to stop.
     */
    STOPPING,

    /**
     * Started state indicates that the unit is fully operational and
     * can be stopped.
     */
    STARTED,

    /**
     * Starting state indicates that the unit is about to start.
     * Usually it is waiting for the units to be started it depends on.
     */
    STARTING
}
