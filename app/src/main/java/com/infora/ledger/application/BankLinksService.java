package com.infora.ledger.application;

import android.util.Log;

import com.infora.ledger.banks.FetchException;
import com.infora.ledger.banks.FetchStrategiesFactory;
import com.infora.ledger.banks.FetchStrategy;
import com.infora.ledger.application.commands.AddBankLinkCommand;
import com.infora.ledger.application.commands.DeleteBankLinksCommand;
import com.infora.ledger.application.commands.FetchBankTransactionsCommand;
import com.infora.ledger.application.commands.UpdateBankLinkCommand;
import com.infora.ledger.application.events.AddBankLinkFailed;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.application.events.BankLinkUpdated;
import com.infora.ledger.application.events.BankLinksDeletedEvent;
import com.infora.ledger.application.events.BankTransactionsFetched;
import com.infora.ledger.application.events.FetchBankTransactionsFailed;
import com.infora.ledger.application.events.UpdateBankLinkFailed;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.data.DatabaseRepository;
import com.infora.ledger.support.Dates;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 31.05.15.
 */
public class BankLinksService {
    private static final String TAG = BankLinksService.class.getName();
    private final EventBus bus;
    private DatabaseContext db;
    private DatabaseRepository<BankLink> repository;

    private FetchStrategiesFactory fetchStrategies;

    public BankLinksService(EventBus bus, DatabaseContext db) {
        this.bus = bus;
        this.db = db;
        this.repository = db.createRepository(BankLink.class);
    }

    public FetchStrategiesFactory getFetchStrategies() {
        return fetchStrategies == null ? (fetchStrategies = FetchStrategiesFactory.createDefault()) : fetchStrategies;
    }

    public void setFetchStrategies(FetchStrategiesFactory fetchStrategies) {
        this.fetchStrategies = fetchStrategies;
    }

    public void onEventBackgroundThread(AddBankLinkCommand command) {
        Log.d(TAG, "Inserting new bank link for bank: " + command.bic + ", account: " + command.accountName);

        Calendar lastSyncDate = Calendar.getInstance();
        lastSyncDate.setTime(command.initialFetchDate);
        lastSyncDate.add(Calendar.DAY_OF_MONTH, -1);
        lastSyncDate.set(Calendar.HOUR, 0);
        lastSyncDate.set(Calendar.MINUTE, 0);
        lastSyncDate.set(Calendar.SECOND, 0);

        BankLink bankLink = new BankLink()
                .setAccountId(command.accountId)
                .setAccountName(command.accountName)
                .setBic(command.bic)
                .setLastSyncDate(lastSyncDate.getTime())
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
            if (command.fetchStartingFrom != null) {
                Log.d(TAG, "Fetch starting from assigned to: " + command.fetchStartingFrom + ". Setting lastSyncDate to previous day.");
                //Transactions are fetched from lastSyncDate + 1.day so setting it to the previous day
                bankLink.lastSyncDate = Dates.addDays(command.fetchStartingFrom, -1);
            }
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

    public void onEventBackgroundThread(FetchBankTransactionsCommand command) {
        Log.d(TAG, "Handling fetch bank transactions command. BankLink id=" + command.bankLinkId);
        try {
            BankLink bankLink = repository.getById(command.bankLinkId);
            fetchBankTransactions(bankLink);
            Log.e(TAG, "Bank transactions fetched. Posting success event.");
            bus.post(new BankTransactionsFetched(bankLink));
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch bank transactions. Posting failure event.", e);
            bus.post(new FetchBankTransactionsFailed(command.bankLinkId, e));
        }
    }

    public void fetchBankTransactions(BankLink bankLink) throws FetchException {
        FetchStrategy fetchStrategy = getFetchStrategies().getStrategy(bankLink.bic);
        fetchStrategy.fetchBankTransactions(db, bankLink);
    }
}
