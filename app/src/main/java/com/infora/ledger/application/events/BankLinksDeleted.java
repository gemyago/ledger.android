package com.infora.ledger.application.events;

/**
 * Created by jenya on 01.06.15.
 */
public class BankLinksDeleted extends Event {
    public final long[] ids;

    public BankLinksDeleted(long[] ids) {
        this.ids = ids;
    }
}
