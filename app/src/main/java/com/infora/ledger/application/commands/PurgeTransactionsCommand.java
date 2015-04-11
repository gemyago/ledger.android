package com.infora.ledger.application.commands;

/**
 * Created by jenya on 10.03.15.
 */
public class PurgeTransactionsCommand {
    private long[] ids;

    public PurgeTransactionsCommand(long... ids) {
        this.ids = ids;
    }

    public long[] getIds() {
        return ids;
    }
}
