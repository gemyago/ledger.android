package com.infora.ledger.application.synchronization;

import android.content.Context;
import android.os.Bundle;

import com.infora.ledger.application.di.DependenciesInjector;
import com.infora.ledger.application.di.DiUtils;
import com.infora.ledger.support.SharedPreferencesProvider;

import javax.inject.Inject;

/**
 * Created by mye on 9/23/2015.
 */
public class SynchronizationStrategiesFactory {
    public static final String OPTION_SYNCHRONIZE_LEDGER_WEB = "option-synchronize-ledger-web";
    public static final String OPTION_SYNC_SINGLE_TRANSACTION = "option-sync-single-transaction";
    public static final String OPTION_SYNC_SINGLE_TRANSACTION_ACTION = "option-sync-single-transaction-action";
    public static final String OPTION_FETCH_BANK_LINKS = "option-fetch-bank-links";

    public static final String SYNC_ACTION_PUBLISH = "sync-action-publish";
    public static final String SYNC_ACTION_ADJUST = "sync-action-adjust";
    public static final String SYNC_ACTION_REJECT = "sync-action-reject";

    private SharedPreferencesProvider prefsProvider;

    @Inject public SynchronizationStrategiesFactory(SharedPreferencesProvider prefsProvider) {
        this.prefsProvider = prefsProvider;
    }

    public SynchronizationStrategy createStrategy(Context context, Bundle options) {
        DependenciesInjector injector = DiUtils.injector(context);
        if (options.getBoolean(OPTION_SYNCHRONIZE_LEDGER_WEB, false))
            return injector.provideLedgerWebSyncStrategy();
        if (options.getBoolean(OPTION_FETCH_BANK_LINKS, false))
            return injector.provideFetchBankLinksSynchronizationStrategy();
        if(options.getInt(OPTION_SYNC_SINGLE_TRANSACTION) != 0)
            return injector.provideLedgerWebPublishReportedSyncStrategy();
        if(prefsProvider.manuallyFetchBankLinks())
            return injector.provideLedgerWebSyncStrategy();
        return new CompositeSynchronizationStrategy(
                injector.provideFetchBankLinksSynchronizationStrategy(),
                injector.provideLedgerWebSyncStrategy());
    }
}
