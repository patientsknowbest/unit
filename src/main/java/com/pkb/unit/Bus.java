package com.pkb.unit;

import com.pkb.unit.message.Message;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * Bus
 * This class serves 2 functions: it provides a communication channel
 * for units to communicate. It also keeps a list of all units IDs
 */
public interface Bus {

    /**
     *
     * @return A Consumer providing an outgoing communication channel for Units to send messages.
     */
    Consumer<Message> sink();

    /**
     *
     * @return An Observable providing an incoming communication channel for Units to receive messages.
     */
    Observable<Message> events();
}
