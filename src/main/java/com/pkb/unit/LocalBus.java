package com.pkb.unit;

import static com.pkb.unit.Filters.payloads;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pkb.unit.message.Message;
import com.pkb.unit.message.payload.NewUnit;

import com.jakewharton.rxrelay2.PublishRelay;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class LocalBus implements Bus, Closeable {
    private PublishRelay<Message> events = PublishRelay.create();
    private List<String> units = new ArrayList<>();
    private Disposable newUnitsSubscription;

    public LocalBus() {
        newUnitsSubscription = payloads(events, NewUnit.class)
                .observeOn(Schedulers.computation())
                .subscribe(nu -> units.add(nu.id()));
    }

    @Override
    public Consumer<Message> sink() {
        return events;
    }

    @Override
    public Observable<Message> events() {
        return events;
    }

    @Override
    public List<String> units() {
        return units;
    }

    @Override
    public void close() throws IOException {
        newUnitsSubscription.dispose();
    }
}
