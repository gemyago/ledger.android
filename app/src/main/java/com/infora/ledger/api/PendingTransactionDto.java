package com.infora.ledger.api;

import com.google.gson.annotations.SerializedName;

/**
 * Created by jenya on 25.03.15.
 */
public class PendingTransactionDto {
    @SerializedName("transaction_id")
    public String transactionId;
    @SerializedName("amount")
    public String amount;
    @SerializedName("comment")
    public String comment;

    public PendingTransactionDto() {
    }

    public PendingTransactionDto(String transactionId, String amount, String comment) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.comment = comment;
    }
}
