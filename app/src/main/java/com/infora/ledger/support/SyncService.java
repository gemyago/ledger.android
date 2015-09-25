package com.infora.ledger.support;

import android.accounts.Account;
import android.content.ContentResolver;
import android.os.Bundle;
import android.util.Log;

import com.infora.ledger.TransactionContract;

import javax.inject.Inject;

/**
 * Created by mye on 9/24/2015.
 */
public class SyncService {
    private static final String TAG = SyncService.class.getName();

    @Inject public SyncService() {
    }

    public void requestSync(Account account, String authority, Bundle extras) {
        ContentResolver.requestSync(account, TransactionContract.AUTHORITY, extras);
    }

    public void addPeriodicSync(Account account, String authority, Bundle extras, long pollFrequency) {
        Log.d(TAG, "Adding periodic sync. Interval: " + pollFrequency);
        ContentResolver.addPeriodicSync(account, authority, extras, pollFrequency);
    }

    public void setSyncAutomatically(Account account, String authority, boolean sync) {
        Log.d(TAG, "Setting sync to: " + sync);
        ContentResolver.setSyncAutomatically(account, authority, sync);
    }
}
