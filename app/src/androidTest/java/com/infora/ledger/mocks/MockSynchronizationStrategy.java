package com.infora.ledger.mocks;

import android.accounts.Account;
import android.content.SyncResult;
import android.os.Bundle;

import com.infora.ledger.application.synchronization.SynchronizationException;
import com.infora.ledger.application.synchronization.SynchronizationStrategy;

/**
 * Created by jenya on 11.04.15.
 */
public class MockSynchronizationStrategy implements SynchronizationStrategy {

    public OnSynchronize onSynchronize;

    @Override
    public void synchronize(Account account, Bundle options, SyncResult syncResult) throws SynchronizationException {
        if (onSynchronize != null) onSynchronize.perform(account, options, syncResult);
    }

    public interface OnSynchronize {
        void perform(Account account, Bundle options, SyncResult syncResult) throws SynchronizationException;
    }
}
