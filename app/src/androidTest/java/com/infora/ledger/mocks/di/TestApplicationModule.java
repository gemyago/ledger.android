package com.infora.ledger.mocks.di;

import android.app.Application;
import android.content.Context;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.api.LedgerApiFactory;
import com.infora.ledger.application.BankLinksService;
import com.infora.ledger.application.DeviceSecretProvider;
import com.infora.ledger.application.PendingTransactionsService;
import com.infora.ledger.application.synchronization.SynchronizationStrategiesFactory;
import com.infora.ledger.banks.AddBankLinkStrategiesFactory;
import com.infora.ledger.banks.ua.privatbank.Privat24BankService;
import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.data.LedgerAccountsLoader;
import com.infora.ledger.data.TransactionsReadModel;
import com.infora.ledger.mocks.MockAccountManagerWrapper;
import com.infora.ledger.mocks.MockDatabaseContext;
import com.infora.ledger.mocks.MockDeviceSecretProvider;
import com.infora.ledger.mocks.MockLedgerApiFactory;
import com.infora.ledger.mocks.MockPendingTransactionsService;
import com.infora.ledger.mocks.MockSharedPrefsProvider;
import com.infora.ledger.mocks.MockSyncService;
import com.infora.ledger.support.AccountManagerWrapper;
import com.infora.ledger.support.GooglePlayServicesUtilWrapper;
import com.infora.ledger.support.SharedPreferencesProvider;
import com.infora.ledger.support.SyncService;
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
    public DatabaseContext databaseContext;
    public SynchronizationStrategiesFactory synchronizationStrategiesFactory;
    public SyncService syncService;
    public MockSharedPrefsProvider sharedPrefsProvider;
    public MockPendingTransactionsService pendingTransactionsService;
    public Privat24BankService privat24BankService;
    private Application app;

    public TestApplicationModule(Application app) {
        this.app = app;
    }

    @Provides Context provideContext() {
        return app;
    }

    @Provides @Singleton
    PendingTransactionsService providePendingTransactionsService() {
        if(pendingTransactionsService == null) pendingTransactionsService = new MockPendingTransactionsService();
        return pendingTransactionsService;
    }

    @Provides @Singleton
    BankLinksService provideBankLinksService(EventBus bus, DatabaseContext db, DeviceSecretProvider secretProvider) {
        return new BankLinksService(bus, db, secretProvider);
    }

    @Provides @Singleton EventBus provideEventBus() {
        return new EventBus();
    }

    @Provides @Singleton
    public GooglePlayServicesUtilWrapper provideGooglePlayServicesUtilWrapper() {
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
        return accountManagerWrapper == null ? new MockAccountManagerWrapper(app) : accountManagerWrapper;
    }

    @Provides TransactionsReadModel provideTransactionsReadModel() {
        return transactionsReadModel;
    }

    @Provides DatabaseContext provideDatabaseContext() {
        return databaseContext == null ? new MockDatabaseContext() : databaseContext;
    }

    @Provides LedgerApiFactory provideApiAdapter() {
        return new MockLedgerApiFactory();
    }

    @Provides SynchronizationStrategiesFactory provideSynchronizationStrategiesFactory(SharedPreferencesProvider prefsProvider) {
        return synchronizationStrategiesFactory == null ? new SynchronizationStrategiesFactory(prefsProvider) : synchronizationStrategiesFactory;
    }

    @Provides SyncService provideSyncService() {
        if(syncService == null) syncService = new MockSyncService();
        return syncService;
    }

    @Provides SharedPreferencesProvider provideSharedPrefsProvider() {
        if(sharedPrefsProvider == null) sharedPrefsProvider = new MockSharedPrefsProvider();
        return sharedPrefsProvider;
    }

    @Provides Privat24BankService providePrivat24BankService() {
        if(privat24BankService == null) privat24BankService = new Privat24BankService();
        return privat24BankService;
    }

    @Provides AddBankLinkStrategiesFactory provideAddBankLinkStrategiesFactory(Context context) {
        return AddBankLinkStrategiesFactory.createDefault(context);
    }
}
