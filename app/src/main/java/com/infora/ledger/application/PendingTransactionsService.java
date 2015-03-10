package com.infora.ledger.application;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import com.infora.ledger.PendingTransaction;
import com.infora.ledger.PendingTransactionContract;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 10.03.15.
 */
public class PendingTransactionsService {
    private static final String TAG = PendingTransactionsService.class.getName();
    private ContentResolver resolver;
    private EventBus bus;

    public PendingTransactionsService(ContentResolver resolver, EventBus bus) {
        this.resolver = resolver;
        this.bus = bus;
    }

    public void onEventBackgroundThread(ReportTransactionCommand command) {
        Log.d(TAG, "Reporting new transaction");
        ContentValues values = PendingTransaction.appendValues(new ContentValues(), command.getAmount(), command.getComment());
        Uri uri = resolver.insert(PendingTransactionContract.CONTENT_URI, values);
        bus.post(new TransactionReportedEvent(ContentUris.parseId(uri)));
    }

    public void onEventBackgroundThread(RemoveTransactionsCommand command) {
        Log.d(TAG, "Removing transactions. Count: " + command.getIds().length);
        for (long id : command.getIds()) {
            resolver.delete(ContentUris.withAppendedId(PendingTransactionContract.CONTENT_URI, id), null, null);
        }
        bus.post(new TransactionsRemovedEvent(command.getIds()));
    }
}
