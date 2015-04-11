package com.infora.ledger.application;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.infora.ledger.LedgerApplication;
import com.infora.ledger.SettingsFragment;
import com.infora.ledger.api.ApiAdapter;
import com.infora.ledger.api.ApiAuthenticator;
import com.infora.ledger.api.AuthenticityToken;
import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.support.AccountManagerWrapper;
import com.infora.ledger.support.SharedPreferencesUtil;

import java.io.IOException;

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
        Log.d(TAG, "Initializing sync adapter...");
        resolver = context.getContentResolver();
        accountManager = new AccountManagerWrapper(context);
        LedgerApplication app = (LedgerApplication) context.getApplicationContext();
        syncStrategy = new FullSyncSynchronizationStrategy(app.getBus());
        SharedPreferences prefs = SharedPreferencesUtil.getDefaultSharedPreferences(context);
        ledgerHost = prefs.getString(SettingsFragment.KEY_LEDGER_HOST, null);
        prefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.d(TAG, "Shared pref changed. Key: " + key);
                if (SettingsFragment.KEY_LEDGER_HOST.equals(key)) {
                    ledgerHost = sharedPreferences.getString(key, null);
                }
            }
        });
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Performing synchronization...");

        Log.d(TAG, "Using ledger host: " + ledgerHost);
        ApiAdapter apiAdapter = new ApiAdapter(accountManager, ledgerHost);
        LedgerApi api = apiAdapter.createApi();
        apiAdapter.authenticateApi(api, account);

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

    public static class PendingTransactionsSyncService extends Service {

        private static final Object initLock = new Object();
        private PendingTransactionsSyncAdapter syncAdapter;

        @Override
        public void onCreate() {
            synchronized (initLock) {
                if(syncAdapter == null) syncAdapter = new PendingTransactionsSyncAdapter(this, true);
            }
        }

        @Override
        public IBinder onBind(Intent intent) {
            return syncAdapter.getSyncAdapterBinder();
        }
    }
}