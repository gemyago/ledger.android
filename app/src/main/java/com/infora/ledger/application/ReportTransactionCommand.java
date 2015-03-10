package com.infora.ledger.application;

/**
 * Created by jenya on 10.03.15.
 */
public class ReportTransactionCommand {
    private String amount;
    private String comment;

    public ReportTransactionCommand(String amount, String comment) {
        this.amount = amount;
        this.comment = comment;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
