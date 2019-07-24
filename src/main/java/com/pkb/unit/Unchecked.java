package com.pkb.unit;

/**
 * Helper class to avoid unwanted exception checks.
 */
public class Unchecked {
    @FunctionalInterface
    public interface RunnableWithThrows {
        void run() throws Exception;
    }

    /**
     * Checks exception thrown by the runnable.
     * @param runnable the code that will be run and can throw checked exception
     */
    public static void unchecked(RunnableWithThrows runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
