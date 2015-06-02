package com.infora.ledger.application;

import android.content.ContentResolver;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.TransactionContract;
import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.api.PendingTransactionDto;
import com.infora.ledger.application.commands.MarkTransactionAsPublishedCommand;
import com.infora.ledger.application.commands.PurgeTransactionsCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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

    public void synchronize(LedgerApi api, ContentResolver resolver, Bundle options, SyncResult syncResult) {
        Log.i(TAG, "Starting full synchronization...");

        Log.d(TAG, "Retrieving pending transactions from the server...");
        List<PendingTransactionDto> remoteTransactions = api.getPendingTransactions();
        HashMap<String, PendingTransactionDto> remoteTransactionsMap = new HashMap<>();
        for (PendingTransactionDto remoteTransaction : remoteTransactions) {
            remoteTransactionsMap.put(remoteTransaction.transactionId, remoteTransaction);
        }

        Log.d(TAG, "Querying locally reported transactions");
        Cursor localTransactions = resolver.query(TransactionContract.CONTENT_URI, null, null, null, null);
        ArrayList<Integer> toPurgeIds = new ArrayList<>();
        while (localTransactions.moveToNext()) {
            PendingTransaction lt = new PendingTransaction(localTransactions);
            if (remoteTransactionsMap.containsKey(lt.transactionId)) {
                if (lt.isDeleted) {
                    Log.d(TAG, "Local transaction '" + lt.transactionId + "' was marked as removed. Rejecting and marking for purge.");
                    api.rejectPendingTransaction(lt.transactionId);
                    toPurgeIds.add(lt.id);
                } else {
                    PendingTransactionDto remoteTransaction = remoteTransactionsMap.get(lt.transactionId);
                    if (!Objects.equals(remoteTransaction.amount, lt.amount) ||
                            !remoteTransaction.comment.equals(lt.comment)) {
                        api.adjustPendingTransaction(lt.transactionId, lt.amount, lt.comment);
                    }
                }
            } else {
                if (lt.isPublished) {
                    Log.d(TAG, "Pending transaction '" + lt.transactionId + "' was approved or rejected. Marking for purge.");
                    toPurgeIds.add(lt.id);
                } else {
                    Log.d(TAG, "Publishing pending transaction: " + lt.transactionId);
                    api.reportPendingTransaction(lt.transactionId, lt.amount, lt.comment, lt.timestamp);
                    bus.post(new MarkTransactionAsPublishedCommand(lt.id));
                }
            }
        }

        if (!toPurgeIds.isEmpty()) {
            long[] ids = new long[toPurgeIds.size()];
            for (int i = 0; i < toPurgeIds.size(); i++) {
                ids[i] = toPurgeIds.get(i);
            }
            Log.d(TAG, "Posting command to purge required transactions...");
            bus.post(new PurgeTransactionsCommand(ids));
        }
    }
}
