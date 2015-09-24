package com.infora.ledger.mocks;

import android.accounts.Account;
import android.os.Bundle;

import com.infora.ledger.support.SyncService;

/**
 * Created by mye on 9/24/2015.
 */
public class MockSyncService extends SyncService {
    public OnRequestSync onRequestSync;

    @Override public void requestSync(Account account, String authority, Bundle extras) {
        if (onRequestSync != null) {
            onRequestSync.call(account, authority, extras);
        }
    }

    public interface OnRequestSync {
        void call(Account account, String authority, Bundle extras);
    }
}
