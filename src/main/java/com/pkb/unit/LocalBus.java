package com.pkb.unit;

import com.jakewharton.rxrelay2.PublishRelay;

import com.pkb.unit.message.Message;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class LocalBus implements Bus {
    private PublishRelay<Message> events = PublishRelay.create();


    @Override
    public Consumer<Message> sink() {
        return events;
    }

    @Override
    public Observable<Message> events() {
        return events;
    }
}
