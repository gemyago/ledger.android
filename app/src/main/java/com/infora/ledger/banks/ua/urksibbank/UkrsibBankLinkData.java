package com.infora.ledger.banks.ua.urksibbank;

import com.infora.ledger.support.ObfuscatedString;

/**
 * Created by mye on 7/7/2015.
 */
public class UkrsibBankLinkData {
    public String login;
    public String password;
    public String account;
    public String card;

    public UkrsibBankLinkData(String login, String password, String account, String card) {
        this.login = login;
        this.password = password;
        this.account = account;
        this.card = card;
    }

    @Override
    public String toString() {
        return "UkrsibBankLinkData{" +
                "login='" + ObfuscatedString.value(login) + '\'' +
                ", password='" + ObfuscatedString.value(password) + '\'' +
                ", account='" + ObfuscatedString.value(account) + '\'' +
                ", card='" + ObfuscatedString.value(card) + '\'' +
                '}';
    }
}
