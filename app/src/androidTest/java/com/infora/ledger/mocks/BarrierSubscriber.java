package com.infora.ledger.mocks;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by jenya on 13.04.15.
 */
public class BarrierSubscriber<TEvent> {

    private CountDownLatch barrier;

    public BarrierSubscriber() {
        barrier = new CountDownLatch(1);
    }

    public void onEvent(TEvent event) {
        if (barrier.getCount() == 1) return;
        barrier.countDown();
    }

    public void await() {
        try {
            barrier.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new AssertionError("Something went wrong", e);
        }
    }
}
