package com.infora.ledger.application.synchronization;

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

import com.infora.ledger.application.di.DiUtils;
import com.infora.ledger.application.events.SynchronizationCompleted;
import com.infora.ledger.application.events.SynchronizationFailed;
import com.infora.ledger.application.events.SynchronizationStarted;

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
    @Inject SynchronizationStrategiesFactory strategiesFactory;
    @Inject EventBus bus;

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
        DiUtils.injector(context).inject(this);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Performing synchronization...");
        bus.post(new SynchronizationStarted());
        SynchronizationStrategy syncStrategy = strategiesFactory.createStrategy(getContext(), extras);
        try {
            syncStrategy.synchronize(account, extras, syncResult);
        } catch (RetrofitError e) {
            syncResult.stats.numIoExceptions++;
            Log.e(TAG, "Synchronization aborted due to some network error.", e);
            bus.post(new SynchronizationFailed());
            return;
        } catch (SQLException e) {
            syncResult.stats.numIoExceptions++;
            Log.e(TAG, "Synchronization aborted due to some unhandled SQL error.", e);
            bus.post(new SynchronizationFailed());
            return;
        }
        bus.post(new SynchronizationCompleted());
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
