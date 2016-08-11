package com.infora.ledger.application.synchronization;

import android.accounts.Account;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.api.LedgerApiFactory;
import com.infora.ledger.api.PendingTransactionDto;
import com.infora.ledger.application.PendingTransactionsService;
import com.infora.ledger.application.commands.DeleteTransactionsCommand;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.data.TransactionsReadModel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 25.03.15.
 */
public class LedgerWebSynchronizationStrategy extends BaseLedgerWebSynchronizationStrategy {
    private static final String TAG = LedgerWebSynchronizationStrategy.class.getName();
    private final EventBus bus;
    private final PendingTransactionsService pendingTransactionsService;
    private final TransactionsReadModel readModel;

    @Inject
    public LedgerWebSynchronizationStrategy(EventBus bus, PendingTransactionsService pendingTransactionsService, TransactionsReadModel readModel, LedgerApiFactory apiFactory) {
        super(apiFactory);
        this.bus = bus;
        this.pendingTransactionsService = pendingTransactionsService;
        this.readModel = readModel;
    }

    public void synchronize(Account account, Bundle options, SyncResult syncResult) throws SynchronizationException {
        Log.i(TAG, "Starting full synchronization...");

        LedgerApi api = createApi(account, syncResult);

        Log.d(TAG, "Retrieving pending transactions from the server...");
        List<PendingTransactionDto> remoteTransactions = api.getPendingTransactions();
        HashMap<String, PendingTransactionDto> remoteTransactionsMap = new HashMap<>();
        for(PendingTransactionDto remoteTransaction : remoteTransactions) {
            remoteTransactionsMap.put(remoteTransaction.transactionId, remoteTransaction);
        }

        ArrayList<Integer> toDeleteIds = new ArrayList<>();
        List<PendingTransaction> localTransactions;
        try {
            localTransactions = readModel.getTransactions();
        } catch(SQLException e) {
            syncResult.stats.numIoExceptions++;
            throw new SynchronizationException("Failed to get local transactions.", e);
        }
        HashMap<String, PendingTransaction> localTransMap = new HashMap<>();
        Log.d(TAG, "Loaded " + localTransactions.size() + " locally reported transactions.");

        for(PendingTransaction localTran : localTransactions) {
            localTransMap.put(localTran.transactionId, localTran);

            if(remoteTransactionsMap.containsKey(localTran.transactionId)) {
                //Updating transactions
                PendingTransactionDto remoteTran = remoteTransactionsMap.get(localTran.transactionId);
                if(!Objects.equals(remoteTran.amount, localTran.amount) ||
                        !remoteTran.comment.equals(localTran.comment)) {
                    adjustPendingTransaction(api, localTran, syncResult);
                } else {
                    Log.d(TAG, "Transaction id='" + localTran.id + "' has not ben changed. Skipping.");
                }
            } else if(localTran.isPublished) {
                //Published transactions that not present locally are approved or rejected so safe to delete locally
                Log.d(TAG, "Local transaction id='" + localTran.id + "' was approved or rejected. Marking for deletion.");
                toDeleteIds.add(localTran.id);
            } else {
                //Transactions not present remotely are new so publishing
                publishPendingTransaction(localTran, syncResult, api, bus);
            }
        }

        try {
            for(PendingTransactionDto remoteTran : remoteTransactionsMap.values()) {
                if(!localTransMap.containsKey(remoteTran.transactionId)) {
                    //Do not reject stuff reported by other services/devices.
                    //localTransMap will not include deleted stuff so have to do this check
                    if(readModel.isTransactionExists(remoteTran.transactionId)) {
                        rejectPendingTransaction(api, remoteTran.transactionId, syncResult);
                    } else {
                        Log.d(TAG, "Remote transaction id='" + remoteTran.transactionId + "' not found locally so skipped.");
                    }
                }
            }
        } catch(SQLException e) {
            syncResult.stats.numIoExceptions++;
            throw new SynchronizationException("Failed to reject remote transactions", e);
        }


        if(!toDeleteIds.isEmpty()) {
            long[] ids = new long[toDeleteIds.size()];
            for(int i = 0; i < toDeleteIds.size(); i++) {
                ids[i] = toDeleteIds.get(i);
            }
            Log.d(TAG, "Posting command to delete '" + toDeleteIds.size() + "' transactions marked for deletion...");
            try {
                pendingTransactionsService.deleteTransactions(new DeleteTransactionsCommand(ids));
            } catch(SQLException e) {
                syncResult.stats.numIoExceptions++;
                throw new SynchronizationException("Failed to delete approved transactions.", e);
            }
        }
    }

}
