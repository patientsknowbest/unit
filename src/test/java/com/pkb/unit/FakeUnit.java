package com.pkb.unit;

import java.util.concurrent.CountDownLatch;

/**
 * A fake
 */
class FakeUnit extends Unit {
    private CountDownLatch cdlCompleteStart = new CountDownLatch(1);
    private CountDownLatch cdlCompleteStop = new CountDownLatch(1);

    public FakeUnit(String id, Bus owner) {
        super(id, owner);
    }

    @Override
    HandleOutcome handleStart() {
        cdlCompleteStart = new CountDownLatch(1);
        System.out.println(id() + " starting");
        try {
            cdlCompleteStart.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(id() + " started successfully");
        return HandleOutcome.SUCCESS;
    }

    @Override
    HandleOutcome handleStop() {
        cdlCompleteStop = new CountDownLatch(1);
        System.out.println(id() + " stopping");
        try {
            cdlCompleteStop.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(id() + " stopped successfully");
        return HandleOutcome.SUCCESS;
    }

    public void completeStart() {
        cdlCompleteStart.countDown();
    }

    public void completeStop() {
        cdlCompleteStop.countDown();
    }
}
