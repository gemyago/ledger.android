package com.infora.ledger.application;

/**
 * Created by jenya on 11.03.15.
 */
public class TransactionReportedEvent {
    private long id;

    public TransactionReportedEvent(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
