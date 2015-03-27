package com.infora.ledger.data;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.infora.ledger.LedgerApplication;
import com.infora.ledger.SettingsFragment;
import com.infora.ledger.api.ApiAdapter;
import com.infora.ledger.api.ApiAuthenticator;
import com.infora.ledger.api.AuthenticityToken;
import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.application.FullSyncSynchronizationStrategy;
import com.infora.ledger.support.AccountManagerWrapper;

import java.io.IOException;

import retrofit.RetrofitError;

/**
 * Created by jenya on 13.03.15.
 */
public class PendingTransactionsSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = PendingTransactionsSyncAdapter.class.getName();

    private ContentResolver resolver;
    private AccountManagerWrapper accountManager;
    private FullSyncSynchronizationStrategy syncStrategy;
    private String ledgerHost;

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
        accountManager = new AccountManagerWrapper(context);
        LedgerApplication app = (LedgerApplication) context.getApplicationContext();
        syncStrategy = new FullSyncSynchronizationStrategy(app.getBus());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        ledgerHost = prefs.getString(SettingsFragment.KEY_LEDGER_HOST, null);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Performing synchronization...");

        Log.d(TAG, "Using ledger host: " + ledgerHost);
        ApiAdapter apiAdapter = new ApiAdapter(ledgerHost);
        LedgerApi api = apiAdapter.getLedgerApi();
        String googleIdToken;
        googleIdToken = tryGettingToken(account, false);
        Log.d(TAG, "Authenticating using google id_token.");
        try {
            tryAuthenticate(apiAdapter, api, googleIdToken);
        } catch (RetrofitError ex) {
            if (ex.getKind() == RetrofitError.Kind.HTTP && ex.getResponse().getStatus() == 401) {
                Log.e(TAG, "Authentication failed. The token might have expired. Invalidating the token and retrying.");
                googleIdToken = tryGettingToken(account, true);
                tryAuthenticate(apiAdapter, api, googleIdToken);
                throw ex;
            } else {
                Log.e(TAG, "Authentication failed. Error kind: " + ex.getKind());
                Log.e(TAG, ex.getMessage());
                throw ex;
            }
        }

        syncStrategy.synchronize(api, resolver, null);

        Log.i(TAG, "Synchronization completed.");
    }

    private String tryGettingToken(Account account, boolean invalidate) {
        String googleIdToken;
        try {
            Log.d(TAG, "Trying to get the token.");
            Bundle options = new Bundle();
            options.putBoolean(ApiAuthenticator.OPTION_INVALIDATE_TOKEN, invalidate);
            googleIdToken = accountManager.getAuthToken(account, options);
        } catch (AuthenticatorException e) {
            //TODO: Implement proper handling
            throw new RuntimeException(e);
        } catch (OperationCanceledException e) {
            //TODO: Implement proper handling
            throw new RuntimeException(e);
        } catch (IOException e) {
            //TODO: Implement proper handling
            throw new RuntimeException(e);
        }
        return googleIdToken;
    }

    private void tryAuthenticate(ApiAdapter apiAdapter, LedgerApi api, String googleIdToken) {
        AuthenticityToken token = api.authenticateByIdToken(googleIdToken);
        apiAdapter.setAuthenticityToken(token.getValue());
    }
}
