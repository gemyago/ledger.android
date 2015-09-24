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

/**
 * Created by mye on 9/24/2015.
 */
public class LedgerWebPublishReportedSyncStrategy extends BaseLedgerWebSynchronizationStrategy {
    private static final String TAG = LedgerWebPublishReportedSyncStrategy.class.getName();
    private final EventBus bus;
    private final TransactionsReadModel readModel;

    @Inject public LedgerWebPublishReportedSyncStrategy(EventBus bus, TransactionsReadModel readModel, LedgerApiFactory apiFactory) {
        super(apiFactory);
        this.bus = bus;
        this.readModel = readModel;
    }

    @Override
    public void synchronize(Account account, Bundle options, SyncResult syncResult) throws SynchronizationException {
        int id = options.getInt(SynchronizationStrategiesFactory.OPTION_PUBLISH_REPORTED_TRANSACTION);
        if(id == 0) throw new SynchronizationException("Transaction id has not been provided.");
        LedgerApi api = createApi(account, syncResult);
        PendingTransaction transaction;
        try {
            transaction = readModel.getById(id);
        } catch (SQLException e) {
            syncResult.stats.numIoExceptions++;
            throw new SynchronizationException(e);
        }
        if(transaction.isPublished) {
            Log.w(TAG, "Transaction id='" + transaction.id + "' has already been published. Publish skipped.");
        } else {
            publishPendingTransaction(transaction, syncResult, api, bus);
        }
    }
}
