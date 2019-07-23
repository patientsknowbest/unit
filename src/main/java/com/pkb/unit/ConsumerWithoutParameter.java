package com.pkb.unit;

import io.reactivex.functions.Consumer;

/**
 * Used at places where the input argument of {@link io.reactivex.functions.Consumer#accept(Object)}
 * is unnecessary and therefore is ignored.
 */
public class ConsumerWithoutParameter {

    /**
     * Takes a Runnable and returns a Consumer that ingores the unnecessary input argument of
     * {@link io.reactivex.functions.Consumer#accept(Object).
     *
     * @param runnable that implements the side effect, the actual cod to be run
     * @return the Consumer that ignores the input argument
     */
    public static  <T> Consumer<? super T> consumer(Runnable runnable) {
        return ignored -> runnable.run();
    }
}
