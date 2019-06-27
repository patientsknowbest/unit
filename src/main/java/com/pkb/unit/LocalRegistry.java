package com.pkb.unit;

import java.util.ArrayList;
import java.util.List;

import com.jakewharton.rxrelay2.PublishRelay;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class LocalRegistry implements Registry {

    private PublishRelay<Message> events = PublishRelay.create();
    private List<Unit> units = new ArrayList<>();
    @Override
    public Consumer<Message> sink() {
        return events;
    }

    @Override
    public Observable<Message> events() {
        return events;
    }

    @Override
    public void register(Unit u) {
        units.add(u);
    }

    @Override
    public List<Unit> units() {
        return units;
    }
}
