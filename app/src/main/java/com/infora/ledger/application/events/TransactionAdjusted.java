package com.infora.ledger.application.events;

/**
 * Created by mye on 9/17/2015.
 */
public class TransactionAdjusted extends Event {
    public long id;

    public TransactionAdjusted(long id) {
        this.id = id;
    }
}
