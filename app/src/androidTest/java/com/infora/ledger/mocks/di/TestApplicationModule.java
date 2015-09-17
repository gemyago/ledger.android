package com.infora.ledger.mocks.di;

import android.app.Application;
import android.content.Context;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.application.BankLinksService;
import com.infora.ledger.application.DeviceSecretProvider;
import com.infora.ledger.application.PendingTransactionsService;
import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.data.LedgerAccountsLoader;
import com.infora.ledger.data.TransactionsReadModel;
import com.infora.ledger.mocks.MockDeviceSecretProvider;
import com.infora.ledger.support.AccountManagerWrapper;
import com.infora.ledger.support.GooglePlayServicesUtilWrapper;
import com.infora.ledger.ui.BankLinkFragmentsFactory;

import java.security.NoSuchAlgorithmException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;

/**
 * Created by mye on 9/16/2015.
 */
@Module
public class TestApplicationModule {

    public GooglePlayServicesUtilWrapper googlePlayServicesUtilWrapper;
    public BankLinkFragmentsFactory bankLinkFragmentsFactory;
    public LedgerAccountsLoader.Factory ledgerAccountsLoaderFactory;
    public DeviceSecretProvider deviceSecretProvider;
    public AccountManagerWrapper accountManagerWrapper;
    public TransactionsReadModel transactionsReadModel;
    private Application app;

    public TestApplicationModule(Application app) {
        this.app = app;
    }

    @Provides Context provideContext() {
        return app;
    }

    @Provides @Singleton
    PendingTransactionsService providePendingTransactionsService(Context context, EventBus bus) {
        return new PendingTransactionsService(context, bus);
    }

    @Provides @Singleton
    BankLinksService provideBankLinksService(EventBus bus, DatabaseContext db, DeviceSecretProvider secretProvider) {
        return new BankLinksService(bus, db, secretProvider);
    }

    @Provides @Singleton EventBus provideEventBus() {
        return new EventBus();
    }

    @Provides @Singleton
    public GooglePlayServicesUtilWrapper provideGooglePlayServicesUtilWrapper(Context context) {
        if (googlePlayServicesUtilWrapper == null) return new GooglePlayServicesUtilWrapper();
        return googlePlayServicesUtilWrapper;
    }

    @Provides public BankLinkFragmentsFactory provideBankLinkFragmentsFactory() {
        return bankLinkFragmentsFactory == null ? new BankLinkFragmentsFactory() : bankLinkFragmentsFactory;
    }

    @Provides public LedgerAccountsLoader.Factory provideLedgerAccountsLoaderFactory() {
        return ledgerAccountsLoaderFactory;
    }

    @Provides public DeviceSecretProvider provideDeviceSecretProvider() {
        try {
            return deviceSecretProvider == null ? new MockDeviceSecretProvider(DeviceSecret.generateNew()) : deviceSecretProvider;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides AccountManagerWrapper provideAccountManagerWrapper() {
        return accountManagerWrapper;
    }

    @Provides TransactionsReadModel provideTransactionsReadModel() {
        return transactionsReadModel;
    }
}
