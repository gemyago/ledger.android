package com.infora.ledger.banks.ua.urksibbank;

import com.infora.ledger.banks.FetchException;

/**
 * Created by mye on 7/8/2015.
 */
public class UkrsibBankException extends FetchException {
    public UkrsibBankException() {
    }

    public UkrsibBankException(String detailMessage) {
        super(detailMessage);
    }

    public UkrsibBankException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UkrsibBankException(Throwable throwable) {
        super(throwable);
    }
}
