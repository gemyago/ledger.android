package com.infora.ledger.application;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.SyncResult;
import android.os.Bundle;
import android.test.AndroidTestCase;

import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.mocks.MockApiAdapter;
import com.infora.ledger.mocks.MockLedgerApi;
import com.infora.ledger.mocks.MockSynchronizationStrategy;

/**
 * Created by jenya on 11.04.15.
 */
public class PendingTransactionsSyncAdapterTest extends AndroidTestCase {

    private PendingTransactionsSyncAdapter subject;
    private MockApiAdapter apiAdapter;
    private MockSynchronizationStrategy syncStrategy;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        subject = new PendingTransactionsSyncAdapter(getContext(), false);
        apiAdapter = new MockApiAdapter();
        subject.setApiAdapter(apiAdapter);
        syncStrategy = new MockSynchronizationStrategy();
        subject.setSyncStrategy(syncStrategy);
    }

    public void testOnPerformSync() throws Exception {
        final Account testAccount = new Account("test-332", "test");
        final MockLedgerApi mockApi = new MockLedgerApi();
        apiAdapter.onAuthenticateApiCallback = new MockApiAdapter.OnAuthenticateApiCallback() {
            @Override
            public void perform(LedgerApi api, Account account) {
                assertSame(mockApi, api);
                assertEquals(testAccount.name, account.name);
            }
        };
        syncStrategy.onSynchronize = new MockSynchronizationStrategy.OnSynchronize() {
            @Override
            public void perform(LedgerApi api, ContentResolver resolver, Bundle options) {
                assertSame(mockApi, api);
            }
        };
        apiAdapter.createdApi = mockApi;
        subject.onPerformSync(testAccount, null, null, null, new SyncResult());
    }
}