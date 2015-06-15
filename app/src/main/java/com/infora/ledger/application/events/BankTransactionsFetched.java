package com.infora.ledger.application.events;

import com.infora.ledger.data.BankLink;

/**
 * Created by jenya on 14.06.15.
 */
public class BankTransactionsFetched {
    public final BankLink bankLink;

    public BankTransactionsFetched(BankLink bankLink) {
        this.bankLink = bankLink;
    }
}
