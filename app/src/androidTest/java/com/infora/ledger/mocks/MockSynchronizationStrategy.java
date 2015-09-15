package com.infora.ledger.mocks;

import android.content.SyncResult;
import android.os.Bundle;

import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.application.SynchronizationStrategy;

/**
 * Created by jenya on 11.04.15.
 */
public class MockSynchronizationStrategy implements SynchronizationStrategy {

    public OnSynchronize onSynchronize;

    @Override
    public void synchronize(LedgerApi api, Bundle options, SyncResult syncResult) {
        if (onSynchronize != null) onSynchronize.perform(api, options, syncResult);
    }

    public interface OnSynchronize {
        void perform(LedgerApi api, Bundle options, SyncResult syncResult);
    }
}
