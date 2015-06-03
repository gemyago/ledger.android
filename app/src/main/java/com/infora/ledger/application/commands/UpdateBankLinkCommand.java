package com.infora.ledger.application.commands;

import com.infora.ledger.banks.PrivatBankLinkData;

/**
 * Created by jenya on 03.06.15.
 */
public class UpdateBankLinkCommand {
    public int id;
    public String accountId;
    public PrivatBankLinkData bankLinkData;

    public UpdateBankLinkCommand(int id, String accountId, PrivatBankLinkData bankLinkData) {
        this.id = id;
        this.accountId = accountId;
        this.bankLinkData = bankLinkData;
    }
}
