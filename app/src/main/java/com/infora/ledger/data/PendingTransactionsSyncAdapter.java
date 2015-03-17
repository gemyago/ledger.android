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
import com.infora.ledger.api.GoogleIdTokensService;
import com.infora.ledger.api.LedgerApi;

import retrofit.RetrofitError;

/**
 * Created by jenya on 13.03.15.
 */
public class PendingTransactionsSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = PendingTransactionsSyncAdapter.class.getName();

    private ContentResolver resolver;
    private GoogleIdTokensService tokensService;

    public PendingTransactionsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        onInit(context);
    }

    public PendingTransactionsSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        onInit(context);
    }

    private void onInit(Context context) {
        tokensService = new GoogleIdTokensService(context);
        resolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Performing synchronization...");

        ApiAdapter apiAdapter = new ApiAdapter("http://10.1.0.19:3000");
        LedgerApi api = apiAdapter.getLedgerApi();
        String googleIdToken = tokensService.getToken();
        Log.d(TAG, "Authenticating using google id_token.");
        try {
            tryAuthenticate(apiAdapter, api, googleIdToken);
        } catch (RetrofitError ex) {
            if (ex.getKind() == RetrofitError.Kind.HTTP && ex.getResponse().getStatus() == 401) {
                Log.e(TAG, "Authentication failed. The token might have expired. Invalidating the token and retrying.");
                tokensService.invalidateToken(googleIdToken);
                tryAuthenticate(apiAdapter, api, tokensService.getToken());
            } else {
                Log.e(TAG, "Authentication failed. Error kind: " + ex.getKind());
                Log.e(TAG, ex.getMessage());
                throw ex;
            }
        }

        Cursor cursor = resolver.query(PendingTransactionContract.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            PendingTransaction t = new PendingTransaction(cursor);
            Log.d(TAG, "Publishing pending transaction: " + t.getTransactionId());
            api.reportPendingTransaction(t.getTransactionId(), t.getAmount(), t.getComment(), t.getTimestamp());
        }

        Log.i(TAG, "Synchronization completed.");
    }

    private void tryAuthenticate(ApiAdapter apiAdapter, LedgerApi api, String googleIdToken) {
        AuthenticityToken token = api.authenticateByIdToken(googleIdToken);
        apiAdapter.setAuthenticityToken(token.getValue());
    }
}
