package com.infora.ledger;

import android.app.Application;
import android.util.Log;

import com.infora.ledger.application.PendingTransactionsService;
import com.squareup.otto.Bus;

/**
 * Created by jenya on 10.03.15.
 */
public class LedgerApplication extends Application {
    private static final String TAG = LedgerApplication.class.getName();
    private Bus bus;

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application created");
        bus = new Bus();

        PendingTransactionsService pendingTransactionsService = new PendingTransactionsService(getContentResolver());
        bus.register(pendingTransactionsService);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "Application terminated");
    }
}
