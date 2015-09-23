package com.infora.ledger.application.synchronization;

import android.accounts.Account;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.test.AndroidTestCase;

import com.infora.ledger.application.events.SynchronizationCompleted;
import com.infora.ledger.application.events.SynchronizationFailed;
import com.infora.ledger.application.events.SynchronizationStarted;
import com.infora.ledger.mocks.MockLedgerApi;
import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.mocks.MockSynchronizationStrategiesFactory;
import com.infora.ledger.mocks.MockSynchronizationStrategy;
import com.infora.ledger.mocks.di.TestApplicationModule;

import java.util.ArrayList;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;

/**
 * Created by jenya on 11.04.15.
 */
public class PendingTransactionsSyncAdapterTest extends AndroidTestCase {
    @Inject EventBus bus;

    private PendingTransactionsSyncAdapter subject;
    private MockSynchronizationStrategy syncStrategy;
    private MockSynchronizationStrategiesFactory synchronizationStrategiesFactory;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        synchronizationStrategiesFactory = new MockSynchronizationStrategiesFactory();
        MockLedgerApplication app = new MockLedgerApplication(getContext())
                .withInjectorModuleInit(new MockLedgerApplication.InjectorModuleInit() {
                    @Override public void init(TestApplicationModule module) {
                        module.synchronizationStrategiesFactory = synchronizationStrategiesFactory;
                    }
                });
        app.injector().inject(this);
        subject = new PendingTransactionsSyncAdapter(app, false);
        syncStrategy = new MockSynchronizationStrategy();
        synchronizationStrategiesFactory.onCreatingStrategy = new MockSynchronizationStrategiesFactory.OnCreatingStrategy() {
            @Override public SynchronizationStrategy call(Context context, Bundle options) {
                return syncStrategy;
            }
        };
    }

    public void testOnPerformSync() throws Exception {
        final Account testAccount = new Account("test-332", "test");
        final SyncResult testSyncResult = new SyncResult();
        final Bundle extras = new Bundle();

        synchronizationStrategiesFactory.onCreatingStrategy = new MockSynchronizationStrategiesFactory.OnCreatingStrategy() {
            @Override public SynchronizationStrategy call(Context context, Bundle options) {
                assertSame(subject.getContext(), context);
                assertSame(extras, options);
                return syncStrategy;
            }
        };

        syncStrategy.onSynchronize = new MockSynchronizationStrategy.OnSynchronize() {
            @Override
            public void perform(Account account, Bundle options, SyncResult syncResult) {
                assertSame(testAccount, account);
                assertSame(options, extras);
                assertSame(testSyncResult, syncResult);
            }
        };

        MockSubscriber<SynchronizationStarted> startedHandler = new MockSubscriber<>(SynchronizationStarted.class);
        MockSubscriber<SynchronizationCompleted> completedHandler = new MockSubscriber<>(SynchronizationCompleted.class);
        bus.register(startedHandler);
        bus.register(completedHandler);
        subject.onPerformSync(testAccount, extras, null, null, testSyncResult);

        assertEquals(1, startedHandler.getEvents().size());
        assertEquals(1, completedHandler.getEvents().size());
    }

    public void testOnPerformSyncWithError() throws Exception {
        final Account testAccount = new Account("test-332", "test");
        final SyncResult testSyncResult = new SyncResult();
        final Bundle extras = new Bundle();
        syncStrategy.onSynchronize = new MockSynchronizationStrategy.OnSynchronize() {
            @Override
            public void perform(Account account, Bundle options, SyncResult syncResult) throws SynchronizationException {
                throw new SynchronizationException("Synchronization failed");
            }
        };
        MockSubscriber<SynchronizationStarted> startedHandler = new MockSubscriber<>(SynchronizationStarted.class);
        MockSubscriber<SynchronizationFailed> failedHandler = new MockSubscriber<>(SynchronizationFailed.class);
        bus.register(startedHandler);
        bus.register(failedHandler);
        subject.onPerformSync(testAccount, extras, null, null, testSyncResult);
        assertEquals(1, startedHandler.getEvents().size());
        assertEquals(1, failedHandler.getEvents().size());
    }
}