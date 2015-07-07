package com.infora.ledger.banks.ua.urksibbank;

/**
 * Created by mye on 7/7/2015.
 */
public class UkrsibBankLinkData {
    public String login;
    public String password;
    public String card;

    public UkrsibBankLinkData(String login, String password, String card) {
        this.login = login;
        this.password = password;
        this.card = card;
    }

    @Override
    public String toString() {
        return "UkrsibBankLinkData{" +
                "login='" + nullOrTrimmedValue(login) + '\'' +
                ", password='" + nullOrTrimmedValue(password) + '\'' +
                ", card='" + nullOrTrimmedValue(card) + '\'' +
                '}';
    }

    private static String nullOrTrimmedValue(String value) {
        return value == null ? "null" : value.charAt(0) + "......" + value.charAt(value.length());
    }
}
