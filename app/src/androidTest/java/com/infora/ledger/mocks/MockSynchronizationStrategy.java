package com.infora.ledger.mocks;

import android.content.ContentResolver;
import android.os.Bundle;

import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.application.SynchronizationStrategy;

/**
 * Created by jenya on 11.04.15.
 */
public class MockSynchronizationStrategy implements SynchronizationStrategy{

    public OnSynchronize onSynchronize;

    @Override
    public void synchronize(LedgerApi api, ContentResolver resolver, Bundle options) {
        if(onSynchronize != null) onSynchronize.perform(api, resolver, options);
    }

    public interface OnSynchronize {
        void perform(LedgerApi api, ContentResolver resolver, Bundle options);
    }
}
