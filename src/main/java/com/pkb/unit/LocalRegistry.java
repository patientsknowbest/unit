package com.pkb.unit;

import com.jakewharton.rxrelay2.PublishRelay;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class LocalRegistry implements Registry {

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
