package com.pkb.unit;

import com.jakewharton.rxrelay2.PublishRelay;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class LocalRegistry implements Registry {

    private PublishRelay<Event<?>> events = PublishRelay.create();

    @Override
    public Consumer<Event<?>> sink() {
        return events;
    }

    @Override
    public Observable<Event<?>> events() {
        return events;
    }
}
