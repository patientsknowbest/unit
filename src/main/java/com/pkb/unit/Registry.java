package com.pkb.unit;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * Registry
 * This class serves 2 functions: it provides a communication channel
 * for units to communicate. It also keeps a list of all units IDs
 */
public interface Registry {

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

    /**
     * Fetch the whole list of units
     * @return
     */
    List<String> units();
}
