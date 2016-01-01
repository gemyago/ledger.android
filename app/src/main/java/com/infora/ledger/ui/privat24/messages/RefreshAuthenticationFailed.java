package com.infora.ledger.ui.privat24.messages;

/**
 * Created by jenya on 01.01.16.
 */
public class RefreshAuthenticationFailed {
    public Exception exception;

    public RefreshAuthenticationFailed(Exception exception) {
        this.exception = exception;
    }
}
