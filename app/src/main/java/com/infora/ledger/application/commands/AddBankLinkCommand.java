package com.infora.ledger.application.commands;

import java.util.Date;

/**
 * Created by jenya on 31.05.15.
 */
public class AddBankLinkCommand<TLinkData> extends Command {
    public String accountId;
    public String accountName;
    public String bic;
    public Date initialFetchDate;
    public TLinkData linkData;
}
