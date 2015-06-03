package com.infora.ledger.application.events;

/**
 * Created by jenya on 03.06.15.
 */
public class UpdateBankLinkFailed {
    public int id;
    public Exception exception;

    public UpdateBankLinkFailed(int id, Exception exception) {
        this.id = id;
        this.exception = exception;
    }
}
