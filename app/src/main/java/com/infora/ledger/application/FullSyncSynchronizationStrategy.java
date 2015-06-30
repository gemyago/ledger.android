package com.infora.ledger.application;

import android.content.ContentResolver;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.api.PendingTransactionDto;
import com.infora.ledger.application.commands.DeleteTransactionsCommand;
import com.infora.ledger.application.commands.MarkTransactionAsPublishedCommand;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.data.TransactionsReadModel;

import java.sql.SQLException;
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
    private TransactionsReadModel readModel;

    public FullSyncSynchronizationStrategy(EventBus bus, TransactionsReadModel readModel) {
        this.bus = bus;
        this.readModel = readModel;
    }

    public void synchronize(LedgerApi api, ContentResolver resolver, Bundle options, SyncResult syncResult) throws SQLException {
        Log.i(TAG, "Starting full synchronization...");

        Log.d(TAG, "Retrieving pending transactions from the server...");
        List<PendingTransactionDto> remoteTransactions = api.getPendingTransactions();
        HashMap<String, PendingTransactionDto> remoteTransactionsMap = new HashMap<>();
        for (PendingTransactionDto remoteTransaction : remoteTransactions) {
            remoteTransactionsMap.put(remoteTransaction.transactionId, remoteTransaction);
        }

        ArrayList<Integer> toDeleteIds = new ArrayList<>();
        List<PendingTransaction> localTransactions = readModel.getTransactions();
        HashMap<String, PendingTransaction> localTransMap = new HashMap<>();
        Log.d(TAG, "Loaded " + localTransactions.size() + " locally reported transactions.");
        for (PendingTransaction localTran : localTransactions) {
            localTransMap.put(localTran.transactionId, localTran);
            if (remoteTransactionsMap.containsKey(localTran.transactionId)) {
                PendingTransactionDto remoteTran = remoteTransactionsMap.get(localTran.transactionId);
                if (!Objects.equals(remoteTran.amount, localTran.amount) ||
                        !remoteTran.comment.equals(localTran.comment)) {
                    Log.d(TAG, "Local transaction id='" + localTran.id + "' has been changed. Adjusting remote.");
                    api.adjustPendingTransaction(localTran.transactionId, localTran.amount, localTran.comment, localTran.accountId);
                } else {
                    Log.d(TAG, "Transaction id='" + localTran.id + "' has not be changed. Skipping.");
                }
            } else if(localTran.isPublished) {
                Log.d(TAG, "Local transaction id='" + localTran.id + "' was approved or rejected. Marking for deletion.");
                toDeleteIds.add(localTran.id);
            } else {
                Log.d(TAG, "Publishing pending transaction: " + localTran.transactionId);
                api.reportPendingTransaction(localTran.transactionId, localTran.amount, localTran.timestamp, localTran.comment, localTran.accountId);
                bus.post(new MarkTransactionAsPublishedCommand(localTran.id));
            }
        }

        for (PendingTransactionDto remoteTran : remoteTransactionsMap.values()) {
            if (!localTransMap.containsKey(remoteTran.transactionId)) {
                Log.d(TAG, "Local transaction '" + remoteTran.transactionId + "' removed. Rejecting remote.");
                api.rejectPendingTransaction(remoteTran.transactionId);
            }
        }

        if (!toDeleteIds.isEmpty()) {
            long[] ids = new long[toDeleteIds.size()];
            for (int i = 0; i < toDeleteIds.size(); i++) {
                ids[i] = toDeleteIds.get(i);
            }
            Log.d(TAG, "Posting command to delete '" + toDeleteIds.size() + "' transactions marked for deletion...");
            bus.post(new DeleteTransactionsCommand(ids));
        }
    }
}
