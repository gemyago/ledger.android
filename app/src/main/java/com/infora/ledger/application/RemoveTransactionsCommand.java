package com.infora.ledger.application;

/**
 * Created by jenya on 10.03.15.
 */
public class RemoveTransactionsCommand {
    private long[] ids;

    public RemoveTransactionsCommand(long... ids) {
        this.ids = ids;
    }

    public long[] getIds() {
        return ids;
    }
}
