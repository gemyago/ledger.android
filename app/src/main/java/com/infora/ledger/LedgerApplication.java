package com.infora.ledger;

import android.app.Application;
import android.util.Log;

import com.infora.ledger.application.PendingTransactionsService;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 10.03.15.
 */
public class LedgerApplication extends Application {
    private static final String TAG = LedgerApplication.class.getName();
    private EventBus bus;

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application created");
        bus = new EventBus();

        PendingTransactionsService pendingTransactionsService = new PendingTransactionsService(getContentResolver());
        bus.register(pendingTransactionsService);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "Application terminated");
    }
}
