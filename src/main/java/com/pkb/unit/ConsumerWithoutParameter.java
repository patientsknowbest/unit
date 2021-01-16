package com.pkb.unit;

import io.reactivex.functions.Consumer;

/**
 * Used at places where the input argument of {@link io.reactivex.functions.Consumer#accept(Object)}
 * is unnecessary and therefore is ignored.
 */
public class ConsumerWithoutParameter {

    /**
     * Takes a Runnable and returns a Consumer that ignores the unnecessary input argument of
     * {@link io.reactivex.functions.Consumer#accept(Object)}.
     *
     * @param runnable that implements the side effect, the actual code to be run
     * @param <T> the type parameter for the consumer
     * @return the Consumer that ignores the input argument
     */
    public static <T> Consumer<? super T> consumer(Runnable runnable) {
        return ignored -> runnable.run();
    }
}
