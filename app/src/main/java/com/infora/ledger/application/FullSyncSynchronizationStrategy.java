package com.infora.ledger.application;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.infora.ledger.PendingTransaction;
import com.infora.ledger.PendingTransactionContract;
import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.api.PendingTransactionDto;

import java.util.ArrayList;
import java.util.HashMap;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 25.03.15.
 */
public class FullSyncSynchronizationStrategy implements SynchronizationStrategy {
    private static final String TAG = FullSyncSynchronizationStrategy.class.getName();
    private final EventBus bus;

    public FullSyncSynchronizationStrategy(EventBus bus) {
        this.bus = bus;
    }

    public void synchronize(LedgerApi api, ContentResolver resolver, Bundle options) {
        Log.i(TAG, "Starting full synchronization...");

        Log.d(TAG, "Retrieving pending transactions from the server...");
        ArrayList<PendingTransactionDto> remoteTransactions = api.getPendingTransactions();
        HashMap<String, PendingTransactionDto> remoteTransactionsMap = new HashMap<>();
        for (PendingTransactionDto remoteTransaction : remoteTransactions) {
            remoteTransactionsMap.put(remoteTransaction.transactionId, remoteTransaction);
        }

        Log.d(TAG, "Querying locally reported transactions");
        Cursor localTransactions = resolver.query(PendingTransactionContract.CONTENT_URI, null, null, null, null);
        while (localTransactions.moveToNext()) {
            PendingTransaction lt = new PendingTransaction(localTransactions);
            if (!remoteTransactionsMap.containsKey(lt.getTransactionId())) {
                Log.d(TAG, "Publishing pending transaction: " + lt.getTransactionId());
                api.reportPendingTransaction(lt.getTransactionId(), lt.getAmount(), lt.getComment(), lt.getTimestamp());
                bus.post(new MarkTransactionAsPublishedCommand(lt.getId()));
            }
        }
    }
}
