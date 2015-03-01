package com.infora.ledger;

/**
 * Created by jenya on 01.03.15.
 */
public class PendingTransaction {
    private String id;
    private String amount;
    private String comment;

    public PendingTransaction() {
    }

    public PendingTransaction(String id, String amount, String comment) {
        this.id = id;
        this.amount = amount;
        this.comment = comment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
