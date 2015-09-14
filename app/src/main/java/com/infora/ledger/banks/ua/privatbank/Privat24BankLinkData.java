package com.infora.ledger.banks.ua.privatbank;

/**
 * Created by mye on 9/10/2015.
 */
public class Privat24BankLinkData {
    public String uniqueId;
    public String login;
    public String password;
    public String cardNumber; //Last 4 digits of the card
    public String cardid;

    /**
     * Will be used instead of imei when using privat24 api.
     * Should be generated for newly created bank links.
     * @return
     */
    public Privat24BankLinkData setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
        return this;
    }

    public Privat24BankLinkData setLogin(String login) {
        this.login = login;
        return this;
    }

    public Privat24BankLinkData setPassword(String password) {
        this.password = password;
        return this;
    }

    public Privat24BankLinkData setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
        return this;
    }

    public Privat24BankLinkData setCardid(String cardid) {
        this.cardid = cardid;
        return this;
    }
}
