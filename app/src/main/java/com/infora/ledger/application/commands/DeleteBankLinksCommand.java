package com.infora.ledger.application.commands;

/**
 * Created by jenya on 01.06.15.
 */
public class DeleteBankLinksCommand extends Command {
    public final long[] ids;

    public DeleteBankLinksCommand(long[] ids) {
        this.ids = ids;
    }
}
