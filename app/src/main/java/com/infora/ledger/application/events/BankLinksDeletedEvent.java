package com.infora.ledger.application.events;

/**
 * Created by jenya on 01.06.15.
 */
public class BankLinksDeletedEvent {
    public final long[] ids;

    public BankLinksDeletedEvent(long[] ids) {
        this.ids = ids;
    }
}
