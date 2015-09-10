package com.infora.ledger.banks.ua.privatbank;

/**
 * Created by mye on 9/10/2015.
 */
public class Privat24BankLinkData {
    public String login;
    public String password;
    public String cookie;

    public Privat24BankLinkData setLogin(String login) {
        this.login = login;
        return this;
    }

    public Privat24BankLinkData setPassword(String password) {
        this.password = password;
        return this;
    }

    public Privat24BankLinkData setCookie(String cookie) {
        this.cookie = cookie;
        return this;
    }
}
