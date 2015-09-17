package com.infora.ledger.application.commands;

/**
 * Created by jenya on 10.03.15.
 */
public class DeleteTransactionsCommand {
    public final long[] ids;

    public DeleteTransactionsCommand(long... ids) {
        this.ids = ids;
    }
}
