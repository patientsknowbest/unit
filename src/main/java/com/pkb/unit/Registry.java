package com.pkb.unit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public interface Registry {

    // We can't have backpressure here
    Consumer<Message> sink();
    Observable<Message> events();
}
