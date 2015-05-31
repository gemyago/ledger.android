package com.infora.ledger.banks;

/**
 * Created by jenya on 30.05.15.
 */
public class PrivatBankLinkData {
    public String card;
    public String merchantId;
    public String password;

    public PrivatBankLinkData(String card, String merchantId, String password) {
        this.card = card;
        this.merchantId = merchantId;
        this.password = password;
    }
}
