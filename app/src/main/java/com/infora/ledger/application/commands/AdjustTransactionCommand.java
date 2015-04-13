package com.infora.ledger.application.commands;

/**
 * Created by jenya on 14.04.15.
 */
public class AdjustTransactionCommand {
    public long id;
    public String amount;
    public String comment;

    public AdjustTransactionCommand(long id, String amount, String comment) {
        this.id = id;
        this.amount = amount;
        this.comment = comment;
    }
}
