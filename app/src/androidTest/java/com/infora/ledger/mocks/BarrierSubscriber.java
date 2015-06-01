package com.infora.ledger.mocks;

import com.infora.ledger.support.LogUtil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by jenya on 13.04.15.
 */
public class BarrierSubscriber<TEvent> {

    private CountDownLatch barrier;
    private Class<TEvent> eventClass;

    public BarrierSubscriber(Class<TEvent> eventClass) {
        this.eventClass = eventClass;
        barrier = new CountDownLatch(1);
    }

    public void onEvent(TEvent event) {
        if(!event.getClass().equals(eventClass)) return;
        LogUtil.d(this, "Barrier event '" + event + "' occurred. Doing countDown...");
        barrier.countDown();
    }

    public void await() {
        try {
            LogUtil.d(this, "Awaiting barrier");
            if(!barrier.await(2, TimeUnit.SECONDS)) {
                throw new AssertionError("Barrier waiting time elapsed.");
            }
            LogUtil.d(this, "Barrier await done.");
        } catch (InterruptedException e) {
            throw new AssertionError("Something went wrong", e);
        }
    }
}
