package com.pkb.unit;

public class Unchecked {
    @FunctionalInterface
    interface RunnableWithThrows {
        void run() throws Exception;
    }

    public static void unchecked(RunnableWithThrows runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
