package com.infora.ledger.banks.ua.urksibbank;

import com.infora.ledger.banks.FetchException;

/**
 * Created by mye on 7/8/2015.
 */
public class UrksibBankException extends FetchException {
    public UrksibBankException() {
    }

    public UrksibBankException(String detailMessage) {
        super(detailMessage);
    }

    public UrksibBankException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UrksibBankException(Throwable throwable) {
        super(throwable);
    }
}
