package com.infora.ledger.application.events;

/**
 * Created by jenya on 14.06.15.
 */
public class FetchBankTransactionsFailed extends Event {
    public final int bankLinkId;
    public final Exception error;

    public FetchBankTransactionsFailed(int bankLinkId, Exception error) {
        this.bankLinkId = bankLinkId;
        this.error = error;
    }
}
