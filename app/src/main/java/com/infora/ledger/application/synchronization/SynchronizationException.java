package com.infora.ledger.application.synchronization;

/**
 * Created by mye on 9/23/2015.
 */
public class SynchronizationException extends Exception {
    public SynchronizationException() {
    }

    public SynchronizationException(String detailMessage) {
        super(detailMessage);
    }

    public SynchronizationException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public SynchronizationException(Throwable throwable) {
        super(throwable);
    }
}
