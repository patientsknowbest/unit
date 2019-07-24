package com.pkb.unit;

import java.io.Serializable;

/**
 * An instruction to a unit sent over the common communication channel
 * wrapped in a {@link com.pkb.unit.message.Message}.
 */
public enum Command implements Serializable {
    START, STOP, ENABLE, DISABLE, CLEAR_DESIRED_STATE
}
