package com.infora.ledger.application.events;

/**
 * Created by jenya on 01.06.15.
 */
public class AddBankLinkFailed {
    public final Exception exception;

    public AddBankLinkFailed(Exception exception) {
        this.exception = exception;
    }
}
