package com.infora.ledger.application;

import com.infora.ledger.application.commands.AddBankLinkCommand;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.BankLinksRepository;

import java.sql.SQLException;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 31.05.15.
 */
public class BankLinksService {
    private final EventBus bus;
    private BankLinksRepository repository;

    public BankLinksService(EventBus bus, BankLinksRepository repository) {
        this.bus = bus;
        this.repository = repository;
    }

    public void onEventBackgroundThread(AddBankLinkCommand command) throws SQLException {
        BankLink bankLink = new BankLink()
                .setAccountId(command.accountId)
                .setAccountName(command.accountName)
                .setBic(command.bic)
                .setLinkData(command.linkData);
        repository.save(bankLink);
        bus.post(new BankLinkAdded(command.accountId, command.bic));
    }
}
