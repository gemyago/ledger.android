package com.infora.ledger.application;

import android.content.ContentResolver;
import android.content.SyncResult;
import android.os.Bundle;

import com.infora.ledger.api.LedgerApi;

/**
 * Created by jenya on 25.03.15.
 */
public interface SynchronizationStrategy {
    void synchronize(LedgerApi api, ContentResolver resolver, Bundle options, SyncResult syncResult);
}
