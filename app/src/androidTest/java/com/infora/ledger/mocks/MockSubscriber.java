package com.infora.ledger.mocks;

/**
 * Created by jenya on 11.03.15.
 */
public class MockSubscriber<TEvent> {
    private TEvent event;

    public TEvent getEvent() {
        return event;
    }

    public void onEvent(TEvent event) {

        this.event = event;
    }
}
