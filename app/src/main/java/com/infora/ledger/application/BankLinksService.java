package com.infora.ledger.application;

import android.util.Log;

import com.infora.ledger.application.commands.AddBankLinkCommand;
import com.infora.ledger.application.commands.DeleteBankLinksCommand;
import com.infora.ledger.application.commands.UpdateBankLinkCommand;
import com.infora.ledger.application.events.AddBankLinkFailed;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.application.events.BankLinkUpdated;
import com.infora.ledger.application.events.BankLinksDeletedEvent;
import com.infora.ledger.application.events.UpdateBankLinkFailed;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.data.DatabaseRepository;

import java.sql.SQLException;
import java.util.Arrays;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 31.05.15.
 */
public class BankLinksService {
    private static final String TAG = BankLinksService.class.getName();
    private final EventBus bus;
    private DatabaseRepository<BankLink> repository;

    public BankLinksService(EventBus bus, DatabaseContext db) {
        this.bus = bus;
        this.repository = db.createRepository(BankLink.class);
    }

    public void onEventBackgroundThread(AddBankLinkCommand command) {
        Log.d(TAG, "Inserting new bank link for bank: " + command.bic + ", account: " + command.accountName);
        BankLink bankLink = new BankLink()
                .setAccountId(command.accountId)
                .setAccountName(command.accountName)
                .setBic(command.bic)
                .setLinkData(command.linkData);
        try {
            repository.save(bankLink);
            bus.post(new BankLinkAdded(command.accountId, command.bic));
        } catch (SQLException e) {
            Log.e(TAG, "Failed to save the bank link.", e);
            bus.post(new AddBankLinkFailed(e));
        }
    }

    public void onEventBackgroundThread(UpdateBankLinkCommand command) {
        Log.d(TAG, "Updating bank link id='" + command.id + "'");
        try {
            BankLink bankLink = repository.getById(command.id);
            bankLink.accountId = command.accountId;
            bankLink.accountName = command.accountName;
            bankLink.setLinkData(command.bankLinkData);
            repository.save(bankLink);
            bus.post(new BankLinkUpdated(command.id));
        } catch (SQLException e) {
            Log.e(TAG, "Failed to update the bank link.", e);
            bus.post(new UpdateBankLinkFailed(command.id, e));
        }
    }

    public void onEventBackgroundThread(DeleteBankLinksCommand command) throws SQLException {
        Log.d(TAG, "Deleting bank links: " + Arrays.toString(command.ids));
        repository.deleteAll(command.ids);
        Log.d(TAG, "Bank links deleted. Posting deleted event...");
        bus.post(new BankLinksDeletedEvent(command.ids));
    }
}
