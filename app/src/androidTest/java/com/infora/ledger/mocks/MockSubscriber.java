package com.infora.ledger.mocks;

import java.util.ArrayList;

/**
 * Created by jenya on 11.03.15.
 */
public class MockSubscriber<TEvent> {
    private TEvent event;
    private Class<TEvent> classOfEvent;

    public MockSubscriber(Class<TEvent> classOfEvent) {
        this.classOfEvent = classOfEvent;
    }

    public TEvent getEvent() {
        return event;
    }

    private ArrayList<TEvent> events = new ArrayList<>();

    public void onEvent(TEvent event) {
        if (event.getClass() != classOfEvent) return;
        events.add(event);
        this.event = event;
    }

    public ArrayList<TEvent> getEvents() {
        return events;
    }
}
