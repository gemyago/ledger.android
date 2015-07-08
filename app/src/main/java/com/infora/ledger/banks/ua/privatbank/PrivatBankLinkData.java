package com.infora.ledger.banks.ua.privatbank;

import com.infora.ledger.support.ObfuscatedString;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrivatBankLinkData that = (PrivatBankLinkData) o;

        if (!card.equals(that.card)) return false;
        if (!merchantId.equals(that.merchantId)) return false;
        return password.equals(that.password);

    }

    @Override
    public int hashCode() {
        int result = card.hashCode();
        result = 31 * result + merchantId.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PrivatBankLinkData{" +
                "card='" + ObfuscatedString.value(card) + '\'' +
                ", merchantId='" + ObfuscatedString.value(merchantId) + '\'' +
                ", password='" + ObfuscatedString.value(password) + '\'' +
                '}';
    }
}
