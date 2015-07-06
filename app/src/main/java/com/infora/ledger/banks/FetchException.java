package com.infora.ledger.banks;

/**
 * Created by jenya on 10.06.15.
 */
public class FetchException extends Exception {
    public FetchException() {
    }

    public FetchException(String detailMessage) {
        super(detailMessage);
    }

    public FetchException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public FetchException(Throwable throwable) {
        super(throwable);
    }
}
