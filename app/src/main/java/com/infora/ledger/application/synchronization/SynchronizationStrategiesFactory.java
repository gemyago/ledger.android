package com.infora.ledger.application.synchronization;

import android.content.Context;
import android.os.Bundle;

import com.infora.ledger.application.di.DiUtils;

import javax.inject.Inject;

/**
 * Created by mye on 9/23/2015.
 */
public class SynchronizationStrategiesFactory {
    @Inject public SynchronizationStrategiesFactory() {
    }

    public SynchronizationStrategy createStrategy(Context context, Bundle options) {
        return DiUtils.injector(context).provideLedgerWebSyncStrategy();
    }
}
