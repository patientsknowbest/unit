package com.pkb.unit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public interface Registry {

    // We can't have backpressure here
    Consumer<Event<?>> sink();
    Observable<Event<?>> events();
}
