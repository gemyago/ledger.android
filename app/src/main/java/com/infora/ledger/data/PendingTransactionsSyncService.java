package com.infora.ledger.data;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by jenya on 13.03.15.
 */
public class PendingTransactionsSyncService extends Service {

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
