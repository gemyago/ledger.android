package com.infora.ledger.application;

/**
 * Created by jenya on 11.03.15.
 */
public class TransactionsRemovedEvent {
    private long[] ids;

    public TransactionsRemovedEvent(long... ids) {
        this.ids = ids;
    }

    public long[] getIds() {
        return ids;
    }
}
