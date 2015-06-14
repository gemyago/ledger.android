package com.infora.ledger.application.commands;

/**
 * Created by jenya on 14.06.15.
 */
public class FetchBankTransactionsCommand {
    public final int bankLinkId;

    public FetchBankTransactionsCommand(int bankLinkId) {
        this.bankLinkId = bankLinkId;
    }
}
