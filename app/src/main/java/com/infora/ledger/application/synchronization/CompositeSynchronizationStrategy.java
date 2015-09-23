package com.infora.ledger.application.synchronization;

import android.accounts.Account;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * Created by mye on 9/23/2015.
 */
public class CompositeSynchronizationStrategy implements SynchronizationStrategy {
    public final SynchronizationStrategy[] strategies;

    public CompositeSynchronizationStrategy(SynchronizationStrategy... strategies) {
        this.strategies = strategies;
    }

    @Override
    public void synchronize(Account account, Bundle options, SyncResult syncResult) throws SynchronizationException {
        for (SynchronizationStrategy strategy : strategies) {
            strategy.synchronize(account, options, syncResult);
        }
    }
}
