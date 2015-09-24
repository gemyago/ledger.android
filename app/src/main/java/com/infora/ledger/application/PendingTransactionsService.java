package com.infora.ledger.application;

import android.util.Log;

import com.infora.ledger.TransactionContract;
import com.infora.ledger.application.commands.AdjustTransactionCommand;
import com.infora.ledger.application.commands.DeleteTransactionsCommand;
import com.infora.ledger.application.commands.MarkTransactionAsPublishedCommand;
import com.infora.ledger.application.commands.ReportTransactionCommand;
import com.infora.ledger.application.events.TransactionAdjusted;
import com.infora.ledger.application.events.TransactionReportedEvent;
import com.infora.ledger.application.events.TransactionsDeletedEvent;
import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.data.DatabaseRepository;
import com.infora.ledger.data.PendingTransaction;

import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 10.03.15.
 */
public class PendingTransactionsService {
    private static final String TAG = PendingTransactionsService.class.getName();
    private final DatabaseRepository<PendingTransaction> repo;
    private EventBus bus;

    public PendingTransactionsService(DatabaseContext db, EventBus bus) {
        repo = db.createRepository(PendingTransaction.class);
        this.bus = bus;
    }

    public void onEventBackgroundThread(ReportTransactionCommand command) throws SQLException {
        PendingTransaction transaction = reportPendingTransaction(command);
        bus.post(new TransactionReportedEvent(transaction.id));
    }

    public PendingTransaction reportPendingTransaction(ReportTransactionCommand command) throws SQLException {
        Log.d(TAG, "Reporting new transaction");
        PendingTransaction transaction = repo.save(new PendingTransaction()
                .setTransactionId(UUID.randomUUID().toString())
                .setTypeId(TransactionContract.TRANSACTION_TYPE_EXPENSE)
                .setAccountId(command.accountId)
                .setAmount(command.amount)
                .setComment(command.comment)
                .setTimestamp(new Date()));
        return transaction;
    }

    public void onEventBackgroundThread(DeleteTransactionsCommand command) throws SQLException {
        deleteTransactions(command);
        bus.post(new TransactionsDeletedEvent(command.ids));
    }

    public void deleteTransactions(DeleteTransactionsCommand command) throws SQLException {
        Log.d(TAG, "Marking transactions as deleted. Count: " + command.ids.length);
        for (long id : command.ids) {
            PendingTransaction transaction = repo.getById(id);
            transaction.isDeleted = true;
            repo.save(transaction);
        }
    }

    public void onEventBackgroundThread(AdjustTransactionCommand command) throws SQLException {
        Log.d(TAG, "Adjusting transaction.");
        PendingTransaction transaction = repo.getById(command.id);
        transaction.amount = command.amount;
        transaction.comment = command.comment;
        repo.save(transaction);
        bus.post(new TransactionAdjusted(command.id));
    }

    public void onEvent(MarkTransactionAsPublishedCommand command) throws SQLException {
        Log.d(TAG, "Processing mark as published command.");
        PendingTransaction transaction = repo.getById(command.id);
        transaction.isPublished = true;
        repo.save(transaction);
    }
}
