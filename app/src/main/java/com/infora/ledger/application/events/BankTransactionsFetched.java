package com.infora.ledger.application.events;

/**
 * Created by jenya on 14.06.15.
 */
public class BankTransactionsFetched {
    public final int bankLinkId;

    public BankTransactionsFetched(int bankLinkId) {
        this.bankLinkId = bankLinkId;
    }
}
