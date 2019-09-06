package com.pkb.unit;

import com.jakewharton.rxrelay2.PublishRelay;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * An implementation of the {@link Bus} interface that serves as the main
 * communication channel for units.
 */
public class LocalBus implements Bus {
    private PublishRelay<Message> events = PublishRelay.create();
    /**
     * @return the event bus messages can be sent to
     */
    @Override
    public Consumer<Message> sink() {
        return events;
    }

    /**
     * @return the event bus to subscribe on where messages are transmitted
     */
    @Override
    public Observable<Message> events() {
        return events;
    }
}
