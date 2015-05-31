package com.infora.ledger.application.events;

/**
 * Created by jenya on 31.05.15.
 */
public class BankLinkAdded {
    public String accountId;
    public String bic;

    public BankLinkAdded(String accountId, String bic) {
        this.accountId = accountId;
        this.bic = bic;
    }
}
