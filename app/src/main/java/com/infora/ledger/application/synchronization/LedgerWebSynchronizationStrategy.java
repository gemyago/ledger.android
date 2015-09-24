package com.infora.ledger.application.synchronization;

import android.accounts.Account;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.api.LedgerApiFactory;
import com.infora.ledger.api.PendingTransactionDto;
import com.infora.ledger.application.commands.DeleteTransactionsCommand;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.data.TransactionsReadModel;
import com.infora.ledger.support.LogUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Created by jenya on 25.03.15.
 */
public class LedgerWebSynchronizationStrategy extends BaseLedgerWebSynchronizationStrategy {
    private static final String TAG = LedgerWebSynchronizationStrategy.class.getName();
    private final EventBus bus;
    private TransactionsReadModel readModel;

    @Inject
    public LedgerWebSynchronizationStrategy(EventBus bus, TransactionsReadModel readModel, LedgerApiFactory apiFactory) {
        super(apiFactory);
        this.bus = bus;
        this.readModel = readModel;
    }

    public void synchronize(Account account, Bundle options, SyncResult syncResult) throws SynchronizationException {
        Log.i(TAG, "Starting full synchronization...");

        LedgerApi api = createApi(account, syncResult);

        Log.d(TAG, "Retrieving pending transactions from the server...");
        List<PendingTransactionDto> remoteTransactions = api.getPendingTransactions();
        HashMap<String, PendingTransactionDto> remoteTransactionsMap = new HashMap<>();
        for (PendingTransactionDto remoteTransaction : remoteTransactions) {
            remoteTransactionsMap.put(remoteTransaction.transactionId, remoteTransaction);
        }

        ArrayList<Integer> toDeleteIds = new ArrayList<>();
        List<PendingTransaction> localTransactions;
        try {
            localTransactions = readModel.getTransactions();
        } catch (SQLException e) {
            syncResult.stats.numIoExceptions++;
            throw new SynchronizationException("Failed to get local transactions.", e);
        }
        HashMap<String, PendingTransaction> localTransMap = new HashMap<>();
        Log.d(TAG, "Loaded " + localTransactions.size() + " locally reported transactions.");


        for (PendingTransaction localTran : localTransactions) {
            localTransMap.put(localTran.transactionId, localTran);
            if (remoteTransactionsMap.containsKey(localTran.transactionId)) {
                PendingTransactionDto remoteTran = remoteTransactionsMap.get(localTran.transactionId);
                if (!Objects.equals(remoteTran.amount, localTran.amount) ||
                        !remoteTran.comment.equals(localTran.comment)) {
                    Log.d(TAG, "Local transaction id='" + localTran.id + "' has been changed. Adjusting remote.");
                    adjustPendingTransaction(api, localTran, syncResult);
                } else {
                    Log.d(TAG, "Transaction id='" + localTran.id + "' has not ben changed. Skipping.");
                }
            } else if (localTran.isPublished) {
                Log.d(TAG, "Local transaction id='" + localTran.id + "' was approved or rejected. Marking for deletion.");
                toDeleteIds.add(localTran.id);
            } else {
                publishPendingTransaction(localTran, syncResult, api, bus);
            }
        }

        for (PendingTransactionDto remoteTran : remoteTransactionsMap.values()) {
            if (!localTransMap.containsKey(remoteTran.transactionId)) {
                Log.d(TAG, "Local transaction '" + remoteTran.transactionId + "' removed. Rejecting remote.");
                rejectPendingTransaction(api, remoteTran, syncResult);
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

    private static void rejectPendingTransaction(LedgerApi api, PendingTransactionDto remoteTran, SyncResult syncResult) throws SynchronizationException {
        try {
            api.rejectPendingTransaction(remoteTran.transactionId);
        } catch (RetrofitError ex) {
            syncResult.stats.numIoExceptions++;
            throw new SynchronizationException("Failed to reject pending transaction." + ex);
        }
    }

    private static void adjustPendingTransaction(LedgerApi api, PendingTransaction localTran, SyncResult syncResult) throws SynchronizationException {
        try {
            api.adjustPendingTransaction(localTran.transactionId, localTran.amount, localTran.comment, localTran.accountId);
        } catch (RetrofitError ex) {
            syncResult.stats.numIoExceptions++;
            throw new SynchronizationException("Failed to adjust pending transaction.", ex);
        }
    }
}
