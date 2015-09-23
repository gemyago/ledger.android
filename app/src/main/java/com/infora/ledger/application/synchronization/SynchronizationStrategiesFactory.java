package com.infora.ledger.application.synchronization;

import android.content.Context;
import android.os.Bundle;

import com.infora.ledger.application.di.DependenciesInjector;
import com.infora.ledger.application.di.DiUtils;

import javax.inject.Inject;

/**
 * Created by mye on 9/23/2015.
 */
public class SynchronizationStrategiesFactory {
    public static final String OPTION_SYNCHRONIZE_LEDGER_WEB = "option-synchronize-ledger-web";
    public static final String OPTION_FETCH_BANK_LINKS = "option-fetch-bank-links";

    @Inject public SynchronizationStrategiesFactory() {
    }

    public SynchronizationStrategy createStrategy(Context context, Bundle options) {
        DependenciesInjector injector = DiUtils.injector(context);
        if (options.getBoolean(OPTION_SYNCHRONIZE_LEDGER_WEB, false))
            return injector.provideLedgerWebSyncStrategy();
        if (options.getBoolean(OPTION_FETCH_BANK_LINKS, false))
            return injector.provideFetchBankLinksSynchronizationStrategy();
        return new CompositeSynchronizationStrategy(
                injector.provideFetchBankLinksSynchronizationStrategy(),
                injector.provideLedgerWebSyncStrategy());
    }
}
