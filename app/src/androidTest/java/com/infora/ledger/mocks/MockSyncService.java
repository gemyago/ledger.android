package com.infora.ledger.mocks;

import android.accounts.Account;
import android.os.Bundle;

import com.infora.ledger.support.SyncService;

/**
 * Created by mye on 9/24/2015.
 */
public class MockSyncService extends SyncService {
    public OnRequestSync onRequestSync;
    public OnSetSyncAutomatically onSetSyncAutomaticallySync;

    @Override public void requestSync(Account account, String authority, Bundle extras) {
        if (onRequestSync != null) {
            onRequestSync.call(account, authority, extras);
        }
    }

    @Override public void setSyncAutomatically(Account account, String authority, boolean sync) {
        if(onSetSyncAutomaticallySync == null) return;
        onSetSyncAutomaticallySync.call(account, authority, sync);
    }

    public interface OnRequestSync {
        void call(Account account, String authority, Bundle extras);
    }

    public interface OnSetSyncAutomatically {
        void call(Account account, String authority, boolean sync);
    }
}
