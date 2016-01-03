package com.infora.ledger.application;

import android.util.Log;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.application.commands.AddBankLinkCommand;
import com.infora.ledger.application.commands.DeleteBankLinksCommand;
import com.infora.ledger.application.commands.FetchBankTransactionsCommand;
import com.infora.ledger.application.commands.UpdateBankLinkCommand;
import com.infora.ledger.application.events.BankLinkUpdated;
import com.infora.ledger.application.events.BankLinksDeleted;
import com.infora.ledger.application.events.BankTransactionsFetched;
import com.infora.ledger.application.events.FetchBankTransactionsFailed;
import com.infora.ledger.application.events.UpdateBankLinkFailed;
import com.infora.ledger.banks.AddBankLinkStrategiesFactory;
import com.infora.ledger.banks.FetchException;
import com.infora.ledger.banks.FetchStrategiesFactory;
import com.infora.ledger.banks.FetchStrategy;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.data.DatabaseRepository;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 31.05.15.
 */
public class BankLinksService {
    private static final String TAG = BankLinksService.class.getName();
    @Inject EventBus bus;
    @Inject DatabaseContext db;
    @Inject DeviceSecretProvider secretProvider;
    @Inject AddBankLinkStrategiesFactory addStrategies;

    private FetchStrategiesFactory fetchStrategies;
    private DatabaseRepository<BankLink> repository;

    @Inject public BankLinksService() {
    }

    public BankLinksService(EventBus bus, DatabaseContext db, DeviceSecretProvider secretProvider) {
        this.bus = bus;
        this.db = db;
        this.secretProvider = secretProvider;
    }

    private DatabaseRepository<BankLink> repository() {
        return this.repository = db.createRepository(BankLink.class);
    }

    public FetchStrategiesFactory getFetchStrategies() {
        return fetchStrategies == null ? (fetchStrategies = FetchStrategiesFactory.createDefault()) : fetchStrategies;
    }

    public void setFetchStrategies(FetchStrategiesFactory fetchStrategies) {
        this.fetchStrategies = fetchStrategies;
    }

    public AddBankLinkStrategiesFactory getAddStrategies() {
        return addStrategies;
    }

    public void setAddStrategies(AddBankLinkStrategiesFactory addStrategies) {
        this.addStrategies = addStrategies;
    }

    public void onEventBackgroundThread(AddBankLinkCommand command) {
        Log.d(TAG, "Inserting new bank link for bank: " + command.bic + ", account: " + command.accountName);
        secretProvider.ensureDeviceRegistered();

        if (command.linkData == null)
            throw new IllegalArgumentException("command.linkData can not be null.");

        DeviceSecret secret = secretProvider.secret();
        BankLink bankLink = new BankLink()
                .setAccountId(command.accountId)
                .setAccountName(command.accountName)
                .setBic(command.bic)
                .setLastSyncDate(command.initialFetchDate)
                .setInitialSyncDate(command.initialFetchDate)
                .setLinkData(command.linkData, secret);

        getAddStrategies().getStrategy(command.bic).addBankLink(bankLink);
    }

    public void onEventBackgroundThread(UpdateBankLinkCommand command) {
        Log.d(TAG, "Updating bank link id='" + command.id + "'");
        secretProvider.ensureDeviceRegistered();
        try {
            BankLink bankLink = repository().getById(command.id);
            bankLink.accountId = command.accountId;
            bankLink.accountName = command.accountName;
            bankLink.setLinkData(command.bankLinkData, secretProvider.secret());
            if (command.fetchStartingFrom != null) {
                Log.d(TAG, "Fetch starting from assigned to: " + command.fetchStartingFrom + ". Setting lastSyncDate and initialSyncDate.");
                bankLink.initialSyncDate = command.fetchStartingFrom;
                bankLink.lastSyncDate = command.fetchStartingFrom;
            }
            repository().save(bankLink);
            bus.post(new BankLinkUpdated(command.id));
        } catch (SQLException e) {
            Log.e(TAG, "Failed to update the bank link.", e);
            bus.post(new UpdateBankLinkFailed(command.id, e));
        }
    }

    public void onEventBackgroundThread(DeleteBankLinksCommand command) throws SQLException {
        Log.d(TAG, "Deleting bank links: " + Arrays.toString(command.ids));
        repository().deleteAll(command.ids);
        Log.d(TAG, "Bank links deleted. Posting deleted event...");
        bus.post(new BankLinksDeleted(command.ids));
    }

    public void onEventBackgroundThread(FetchBankTransactionsCommand command) {
        Log.d(TAG, "Handling fetch bank transactions command. BankLink id=" + command.bankLinkId);
        try {
            BankLink bankLink = repository().getById(command.bankLinkId);
            fetchBankTransactions(bankLink);
            Log.e(TAG, "Bank transactions fetched. Posting success event.");
            bus.post(new BankTransactionsFetched(bankLink));
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch bank transactions. Posting failure event.", e);
            bus.post(new FetchBankTransactionsFailed(command.bankLinkId, e));
        }
    }

    public void fetchAllBankLinks() throws FetchException {
        Log.i(TAG, "Fetching all bank links...");
        List<BankLink> allLinks;
        try {
            allLinks = repository().getAll();
        } catch (SQLException e) {
            throw new FetchException("Failed to get all bank links from the database.", e);
        }
        for (BankLink link : allLinks) {
            fetchBankTransactions(link);
        }
    }

    public void fetchBankTransactions(BankLink bankLink) throws FetchException {
        secretProvider.ensureDeviceRegistered();
        FetchStrategy fetchStrategy = getFetchStrategies().getStrategy(bankLink.bic);
        fetchStrategy.fetchBankTransactions(db, bankLink, secretProvider.secret());
    }
}
