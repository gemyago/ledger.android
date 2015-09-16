package com.infora.ledger.application;

import android.accounts.Account;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.infora.ledger.api.ApiAdapter;
import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.application.di.DiUtils;
import com.infora.ledger.data.TransactionsReadModel;
import com.infora.ledger.support.AccountManagerWrapper;

import java.sql.SQLException;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Created by jenya on 13.03.15.
 */
public class PendingTransactionsSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = PendingTransactionsSyncAdapter.class.getName();

    private ContentResolver resolver;
    private SynchronizationStrategy syncStrategy;
    private ApiAdapter apiAdapter;
    @Inject EventBus bus;

    public PendingTransactionsSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        onInit(context);
    }

    public PendingTransactionsSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        onInit(context);
    }

    public void setSyncStrategy(SynchronizationStrategy syncStrategy) {
        this.syncStrategy = syncStrategy;
    }

    public void setApiAdapter(ApiAdapter apiAdapter) {
        this.apiAdapter = apiAdapter;
    }

    private void onInit(Context context) {
        Log.d(TAG, "Initializing sync adapter...");
        resolver = context.getContentResolver();
        DiUtils.injector(context).inject(this);
        syncStrategy = new FullSyncSynchronizationStrategy(bus, new TransactionsReadModel(context));
        apiAdapter = ApiAdapter.createAdapter(context, new AccountManagerWrapper(context));
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Performing synchronization...");
        LedgerApi api = apiAdapter.createApi();
        try {
            apiAdapter.authenticateApi(api, account);
        } catch (RetrofitError error) {
            syncResult.stats.numAuthExceptions++;
            Log.e(TAG, "Authentication failed. Synchronization aborted.");
            return;
        }
        try {
            syncStrategy.synchronize(api, extras, syncResult);
        } catch (RetrofitError e) {
            syncResult.stats.numIoExceptions++;
            Log.e(TAG, "Synchronization aborted due to some network error.", e);
            return;
        } catch (SQLException e) {
            syncResult.stats.numIoExceptions++;
            Log.e(TAG, "Synchronization aborted due to some unhandled SQL error.", e);
            return;
        }
        Log.i(TAG, "Synchronization completed.");
    }

    public static class PendingTransactionsSyncService extends Service {

        private static final Object initLock = new Object();
        private PendingTransactionsSyncAdapter syncAdapter;

        @Override
        public void onCreate() {
            synchronized (initLock) {
                if (syncAdapter == null)
                    syncAdapter = new PendingTransactionsSyncAdapter(this, true);
            }
        }

        @Override
        public IBinder onBind(Intent intent) {
            return syncAdapter.getSyncAdapterBinder();
        }
    }
}
