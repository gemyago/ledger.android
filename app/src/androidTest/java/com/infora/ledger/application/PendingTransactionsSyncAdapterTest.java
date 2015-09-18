package com.infora.ledger.application;

import android.accounts.Account;
import android.content.SyncResult;
import android.os.Bundle;
import android.test.AndroidTestCase;

import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.application.events.SynchronizationCompleted;
import com.infora.ledger.application.events.SynchronizationFailed;
import com.infora.ledger.application.events.SynchronizationStarted;
import com.infora.ledger.mocks.MockApiAdapter;
import com.infora.ledger.mocks.MockLedgerApi;
import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.mocks.MockSynchronizationStrategy;

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
    private MockApiAdapter apiAdapter;
    private MockSynchronizationStrategy syncStrategy;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        MockLedgerApplication app = new MockLedgerApplication(getContext());
        app.injector().inject(this);
        subject = new PendingTransactionsSyncAdapter(app, false);
        apiAdapter = new MockApiAdapter();
        subject.setApiAdapter(apiAdapter);
        syncStrategy = new MockSynchronizationStrategy();
        subject.setSyncStrategy(syncStrategy);
    }

    public void testOnPerformSync() throws Exception {
        final Account testAccount = new Account("test-332", "test");
        final MockLedgerApi mockApi = new MockLedgerApi();
        final SyncResult testSyncResult = new SyncResult();
        final Bundle extras = new Bundle();
        apiAdapter.onAuthenticateApiCallback = new MockApiAdapter.OnAuthenticateApiCallback() {
            @Override
            public void perform(LedgerApi api, Account account) {
                assertSame(mockApi, api);
                assertEquals(testAccount.name, account.name);
            }
        };
        syncStrategy.onSynchronize = new MockSynchronizationStrategy.OnSynchronize() {
            @Override
            public void perform(LedgerApi api, Bundle options, SyncResult syncResult) {
                assertSame(mockApi, api);
                assertSame(options, extras);
                assertSame(testSyncResult, syncResult);
            }
        };
        apiAdapter.createdApi = mockApi;

        MockSubscriber<SynchronizationStarted> startedHandler = new MockSubscriber<>(SynchronizationStarted.class);
        MockSubscriber<SynchronizationCompleted> completedHandler = new MockSubscriber<>(SynchronizationCompleted.class);
        bus.register(startedHandler);
        bus.register(completedHandler);
        subject.onPerformSync(testAccount, extras, null, null, testSyncResult);

        assertEquals(1, startedHandler.getEvents().size());
        assertEquals(1, completedHandler.getEvents().size());
    }

    public void testOnPerformSyncAuthError() throws Exception {
        final Account testAccount = new Account("test-332", "test");
        final MockLedgerApi mockApi = new MockLedgerApi();
        final SyncResult testSyncResult = new SyncResult();
        final Bundle extras = new Bundle();
        apiAdapter.onAuthenticateApiCallback = new MockApiAdapter.OnAuthenticateApiCallback() {
            @Override
            public void perform(LedgerApi api, Account account) {
                Response response = new Response("localhost", 401, "Unauthorized", new ArrayList<Header>(), null);
                throw RetrofitError.httpError("test", response, null, null);
            }
        };
        syncStrategy.onSynchronize = new MockSynchronizationStrategy.OnSynchronize() {
            @Override
            public void perform(LedgerApi api, Bundle options, SyncResult syncResult) {
                assertFalse("Should not be performed", true);
            }
        };
        apiAdapter.createdApi = mockApi;
        MockSubscriber<SynchronizationStarted> startedHandler = new MockSubscriber<>(SynchronizationStarted.class);
        MockSubscriber<SynchronizationFailed> failedHandler = new MockSubscriber<>(SynchronizationFailed.class);
        bus.register(startedHandler);
        bus.register(failedHandler);

        subject.onPerformSync(testAccount, extras, null, null, testSyncResult);
        assertEquals(1, testSyncResult.stats.numAuthExceptions);

        assertEquals(1, startedHandler.getEvents().size());
        assertEquals(1, failedHandler.getEvents().size());
    }

    public void testOnPerformSyncNetworkError() throws Exception {
        final Account testAccount = new Account("test-332", "test");
        final MockLedgerApi mockApi = new MockLedgerApi();
        final SyncResult testSyncResult = new SyncResult();
        final Bundle extras = new Bundle();
        apiAdapter.onAuthenticateApiCallback = new MockApiAdapter.OnAuthenticateApiCallback() {
            @Override
            public void perform(LedgerApi api, Account account) {
            }
        };
        syncStrategy.onSynchronize = new MockSynchronizationStrategy.OnSynchronize() {
            @Override
            public void perform(LedgerApi api, Bundle options, SyncResult syncResult) {
                Response response = new Response("localhost", 500, "Internal server error", new ArrayList<Header>(), null);
                throw RetrofitError.httpError("test", response, null, null);
            }
        };
        apiAdapter.createdApi = mockApi;
        MockSubscriber<SynchronizationStarted> startedHandler = new MockSubscriber<>(SynchronizationStarted.class);
        MockSubscriber<SynchronizationFailed> failedHandler = new MockSubscriber<>(SynchronizationFailed.class);
        bus.register(startedHandler);
        bus.register(failedHandler);
        subject.onPerformSync(testAccount, extras, null, null, testSyncResult);
        assertEquals(1, testSyncResult.stats.numIoExceptions);
        assertEquals(1, startedHandler.getEvents().size());
        assertEquals(1, failedHandler.getEvents().size());
    }
}