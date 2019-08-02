package com.pkb.unit;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A fake
 */
public class FakeUnit extends Unit {
    public static long RETRY_PERIOD = 1;
    public static TimeUnit RETRY_PERIOD_UNIT = TimeUnit.SECONDS;

    private CountDownLatch cdlCompleteStart = new CountDownLatch(1);
    private CountDownLatch cdlCompleteStop = new CountDownLatch(1);
    private boolean shouldFailStart;
    private boolean shouldFailStop;

    public FakeUnit(String id, Bus owner) {
        super(id, owner, RETRY_PERIOD, RETRY_PERIOD_UNIT);
    }

    @Override
    protected HandleOutcome handleStart() {
        System.out.println(id() + " starting");
        try {
            cdlCompleteStart.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (shouldFailStart) {
            System.out.println(id() + " start failed");
            return HandleOutcome.FAILURE;
        }
        System.out.println(id() + " started successfully");
        return HandleOutcome.SUCCESS;
    }

    @Override
    protected HandleOutcome handleStop() {
        System.out.println(id() + " stopping");
        try {
            cdlCompleteStop.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (shouldFailStop) {
            System.out.println(id() + " stop failed");
            return HandleOutcome.FAILURE;
        }
        System.out.println(id() + " stopped successfully");
        return HandleOutcome.SUCCESS;
    }

    public void completeStart() {
        shouldFailStart = false;
        cdlCompleteStart.countDown();
    }

    public void completeStop() {
        shouldFailStop = false;
        cdlCompleteStop.countDown();
    }
    public void failStart() {
        shouldFailStart = true;
        cdlCompleteStart.countDown();
    }

    public void failStop() {
        shouldFailStop = true;
        cdlCompleteStop.countDown();
    }

    /**
     * Increase the visibility of this to public for testing.
     */
    @Override
    public void failed() {
        super.failed();
    }
}
