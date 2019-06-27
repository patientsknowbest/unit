package com.pkb.unit;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public interface Registry {

    // We can't have backpressure here
    Consumer<Message> sink();
    Observable<Message> events();

    // So we can pull the whole graph of units
    void register(Unit u);
    List<Unit> units();
}
