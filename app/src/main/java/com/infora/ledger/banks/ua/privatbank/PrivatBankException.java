package com.infora.ledger.banks.ua.privatbank;

/**
 * Created by jenya on 24.05.15.
 */
public class PrivatBankException extends Exception {
    public PrivatBankException() {
    }

    public PrivatBankException(String detailMessage) {
        super(detailMessage);
    }

    public PrivatBankException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public PrivatBankException(Throwable throwable) {
        super(throwable);
    }
}
