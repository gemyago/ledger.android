package com.infora.ledger.application.synchronization;

import android.accounts.Account;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.api.LedgerApiFactory;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.data.TransactionsReadModel;

import java.sql.SQLException;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

import static com.infora.ledger.application.synchronization.SynchronizationStrategiesFactory.OPTION_SYNC_SINGLE_TRANSACTION;
import static com.infora.ledger.application.synchronization.SynchronizationStrategiesFactory.OPTION_SYNC_SINGLE_TRANSACTION_ACTION;
import static com.infora.ledger.application.synchronization.SynchronizationStrategiesFactory.SYNC_ACTION_ADJUST;
import static com.infora.ledger.application.synchronization.SynchronizationStrategiesFactory.SYNC_ACTION_PUBLISH;
import static com.infora.ledger.application.synchronization.SynchronizationStrategiesFactory.SYNC_ACTION_REJECT;

/**
 * Created by mye on 9/24/2015.
 */
public class LedgerWebSingleTransactionSyncStrategy extends BaseLedgerWebSynchronizationStrategy {
    private static final String TAG = LedgerWebSingleTransactionSyncStrategy.class.getName();
    private final EventBus bus;
    private final TransactionsReadModel readModel;

    @Inject
    public LedgerWebSingleTransactionSyncStrategy(EventBus bus, TransactionsReadModel readModel, LedgerApiFactory apiFactory) {
        super(apiFactory);
        this.bus = bus;
        this.readModel = readModel;
    }

    @Override
    public void synchronize(Account account, Bundle options, SyncResult syncResult) throws SynchronizationException {
        int id = options.getInt(OPTION_SYNC_SINGLE_TRANSACTION);
        if (id == 0) {
            syncResult.stats.numParseExceptions++;
            throw new SynchronizationException("Transaction id has not been provided.");
        }
        String action = options.getString(OPTION_SYNC_SINGLE_TRANSACTION_ACTION);
        if (action == null) {
            syncResult.stats.numParseExceptions++;
            throw new SynchronizationException("Sync action has not been provided.");
        }

        LedgerApi api = createApi(account, syncResult);
        PendingTransaction transaction;
        try {
            transaction = readModel.getById(id);
        } catch (SQLException e) {
            syncResult.stats.numIoExceptions++;
            throw new SynchronizationException(e);
        }
        Log.i(TAG, "Performing sync action: " + action);
        if (action.equals(SYNC_ACTION_PUBLISH)) {
            if (transaction.isPublished) {
                Log.w(TAG, "Transaction id='" + transaction.id + "' has already been published. Publish skipped.");
            } else {
                publishPendingTransaction(transaction, syncResult, api, bus);
            }
        } else if (action.equals(SYNC_ACTION_ADJUST)) {
            adjustPendingTransaction(api, transaction, syncResult);
        } else if (action.equals(SYNC_ACTION_REJECT)) {
            rejectPendingTransaction(api, transaction.transactionId, syncResult);
        } else {
            syncResult.stats.numParseExceptions++;
            throw new SynchronizationException("Unknown sync action '" + action + "'.");
        }
    }
}
