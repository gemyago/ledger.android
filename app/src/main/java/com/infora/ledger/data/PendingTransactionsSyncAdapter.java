package com.infora.ledger.data;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.infora.ledger.PendingTransaction;
import com.infora.ledger.PendingTransactionContract;
import com.infora.ledger.api.ApiAdapter;
import com.infora.ledger.api.AuthenticityToken;
import com.infora.ledger.api.LedgerApi;

/**
 * Created by jenya on 13.03.15.
 */
public class PendingTransactionsSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = PendingTransactionsSyncAdapter.class.getName();

    private ContentResolver resolver;

    public PendingTransactionsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        onInit(context);
    }

    public PendingTransactionsSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        onInit(context);
    }

    private void onInit(Context context) {
        resolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Performing synchronization...");

        ApiAdapter apiAdapter = new ApiAdapter("http://rj45:3000");
        LedgerApi api = apiAdapter.getLedgerApi();
        AuthenticityToken token = api.authenticate("dev@domain.com", "password");
        apiAdapter.setAuthenticityToken(token.getValue());

        Cursor cursor = resolver.query(PendingTransactionContract.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            PendingTransaction t = new PendingTransaction(cursor);
            Log.d(TAG, "Publishing pending transaction: " + t.getTransactionId());
            api.reportPendingTransaction(t.getTransactionId(), t.getAmount(), t.getComment(), t.getTimestamp());
        }

        Log.i(TAG, "Synchronization completed.");
    }
}
