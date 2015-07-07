package com.infora.ledger.application.commands;

import java.util.Date;

/**
 * Created by jenya on 03.06.15.
 */
public class UpdateBankLinkCommand<TLinkData> {
    public int id;
    public String accountId;
    public String accountName;
    public TLinkData bankLinkData;
    public Date fetchStartingFrom;

    public UpdateBankLinkCommand(int id, String accountId, String accountName, TLinkData bankLinkData) {
        this.id = id;
        this.accountId = accountId;
        this.accountName = accountName;
        this.bankLinkData = bankLinkData;
    }

    public UpdateBankLinkCommand setFetchFromDate(Date value) {
        fetchStartingFrom = value;
        return this;
    }
}
