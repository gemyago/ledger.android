package com.infora.ledger.application.commands;

/**
 * Created by jenya on 31.05.15.
 */
public class AddBankLinkCommand<TLinkData> {
    public String accountId;
    public String bic;
    public TLinkData linkData;
}
