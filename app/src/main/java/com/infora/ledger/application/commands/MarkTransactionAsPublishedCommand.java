package com.infora.ledger.application.commands;

/**
 * Created by jenya on 26.03.15.
 */
public class MarkTransactionAsPublishedCommand extends Command {
    public final long id;

    public MarkTransactionAsPublishedCommand(long id) {
        this.id = id;
    }
}
