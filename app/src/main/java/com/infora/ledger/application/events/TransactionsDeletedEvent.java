package com.infora.ledger.application.events;

/**
 * Created by jenya on 11.03.15.
 */
public class TransactionsDeletedEvent extends Event {
    private long[] ids;

    public TransactionsDeletedEvent(long... ids) {
        this.ids = ids;
    }

    public long[] getIds() {
        return ids;
    }
}
