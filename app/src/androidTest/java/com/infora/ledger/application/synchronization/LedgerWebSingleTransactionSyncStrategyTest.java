package com.infora.ledger.application.synchronization;

import android.accounts.Account;
import android.content.SyncResult;
import android.os.Bundle;
import android.test.AndroidTestCase;

import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.api.PendingTransactionDto;
import com.infora.ledger.application.commands.MarkTransactionAsPublishedCommand;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.mocks.MockLedgerApi;
import com.infora.ledger.mocks.MockLedgerApiFactory;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.mocks.MockTransactionsReadModel;

import java.util.ArrayList;
import java.util.Date;

import de.greenrobot.event.EventBus;

import static com.infora.ledger.TransactionContract.TRANSACTION_TYPE_EXPENSE;
import static com.infora.ledger.application.synchronization.SynchronizationStrategiesFactory.*;
import static com.infora.ledger.application.synchronization.SynchronizationStrategiesFactory.OPTION_SYNC_SINGLE_TRANSACTION;
import static com.infora.ledger.application.synchronization.SynchronizationStrategiesFactory.OPTION_SYNC_SINGLE_TRANSACTION_ACTION;

/**
 * Created by jenya on 25.03.15.
 */
public class LedgerWebSingleTransactionSyncStrategyTest extends AndroidTestCase {

    private MockTransactionsReadModel readModel;
    private LedgerWebSingleTransactionSyncStrategy subject;
    private MockLedgerApi api;
    private EventBus bus;
    private SyncResult syncResult;
    private Account account;
    private MockLedgerApiFactory apiFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        bus = new EventBus();
        readModel = new MockTransactionsReadModel();
        account = new Account("test-332", "test");
        api = new MockLedgerApi();
        apiFactory = new MockLedgerApiFactory();
        apiFactory.onCreatingApi = new MockLedgerApiFactory.OnCreatingApi() {
            @Override public LedgerApi call(Account actualAccount) {
                assertSame(account, actualAccount);
                return api;
            }
        };
        subject = new LedgerWebSingleTransactionSyncStrategy(bus, readModel, apiFactory);
        syncResult = new SyncResult();
    }

    public void testSynchronizeThrowErrorIfNoTransactionId() throws Exception {
        Bundle options = new Bundle();
        boolean isRaised = false;
        try {
            subject.synchronize(account, options, syncResult);
        } catch (SynchronizationException ex) {
            assertEquals("Transaction id has not been provided.", ex.getMessage());
            isRaised = true;
        }
        assertTrue(isRaised);
        assertEquals(1, syncResult.stats.numParseExceptions);
    }

    public void testSynchronizeThrowErrorIfNoAction() throws Exception {
        Bundle options = new Bundle();
        options.putInt(OPTION_SYNC_SINGLE_TRANSACTION, 100);
        boolean isRaised = false;
        try {
            subject.synchronize(account, options, syncResult);
        } catch (SynchronizationException ex) {
            assertEquals("Sync action has not been provided.", ex.getMessage());
            isRaised = true;
        }
        assertTrue(isRaised);
        assertEquals(1, syncResult.stats.numParseExceptions);
    }

    public void testSynchronizeThrowErrorIfUnknownAction() throws Exception {
        PendingTransaction t1 = readModel.inject(new PendingTransaction("t-1", "100", "t 100", false, false, new Date(), null)
                .setId(112233).setAccountId("account-1").setTypeId(TRANSACTION_TYPE_EXPENSE));

        Bundle options = new Bundle();
        options.putInt(OPTION_SYNC_SINGLE_TRANSACTION, t1.id);
        options.putString(OPTION_SYNC_SINGLE_TRANSACTION_ACTION, "unknown-action");
        boolean isRaised = false;
        try {
            subject.synchronize(account, options, syncResult);
        } catch (SynchronizationException ex) {
            assertEquals("Unknown sync action 'unknown-action'.", ex.getMessage());
            isRaised = true;
        }
        assertTrue(isRaised);
        assertEquals(1, syncResult.stats.numParseExceptions);
    }

    public void testSynchronizeReportNew() throws Exception {
        api.setPendingTransactions(new ArrayList<PendingTransactionDto>());

        PendingTransaction t1 = readModel.inject(new PendingTransaction("t-1", "100", "t 100", false, false, new Date(), null)
                .setId(112233).setAccountId("account-1").setTypeId(TRANSACTION_TYPE_EXPENSE));
        MockSubscriber<MarkTransactionAsPublishedCommand> publishedSubscriber = new MockSubscriber<>(MarkTransactionAsPublishedCommand.class);
        bus.register(publishedSubscriber);

        Bundle options = new Bundle();
        options.putInt(OPTION_SYNC_SINGLE_TRANSACTION, t1.getId());
        options.putString(OPTION_SYNC_SINGLE_TRANSACTION_ACTION, SYNC_ACTION_PUBLISH);
        subject.synchronize(account, options, syncResult);

        assertEquals(1, api.getReportedTransactions().size());

        PendingTransaction reported1 = api.getReportedTransactions().get(0).toTransaction().setId(t1.id);
        assertEquals(t1, reported1);

        assertEquals(1, publishedSubscriber.getEvents().size());
        assertEquals(t1.getId(), publishedSubscriber.getEvents().get(0).id);
    }

    public void testSynchronizeSkipPublished() throws Exception {
        api.setPendingTransactions(new ArrayList<PendingTransactionDto>());

        PendingTransaction t1 = readModel.inject(new PendingTransaction("t-1", "100", "t 100", true, false, new Date(), null)
                .setId(112233).setAccountId("account-1").setTypeId(TRANSACTION_TYPE_EXPENSE));
        MockSubscriber<MarkTransactionAsPublishedCommand> publishedSubscriber = new MockSubscriber<>(MarkTransactionAsPublishedCommand.class);
        bus.register(publishedSubscriber);

        Bundle options = new Bundle();
        options.putInt(OPTION_SYNC_SINGLE_TRANSACTION, t1.getId());
        options.putString(OPTION_SYNC_SINGLE_TRANSACTION_ACTION, SYNC_ACTION_PUBLISH);
        subject.synchronize(account, options, syncResult);

        assertEquals(0, api.getReportedTransactions().size());
        assertEquals(0, publishedSubscriber.getEvents().size());
    }

    public void testSynchronizeAdjustChanged() throws Exception {
        api.setPendingTransactions(new ArrayList<PendingTransactionDto>());

        PendingTransaction t1 = readModel.inject(new PendingTransaction("t-1", "100", "t 100", false, false, new Date(), null)
                .setId(112233).setAccountId("account-1").setTypeId(TRANSACTION_TYPE_EXPENSE));

        Bundle options = new Bundle();
        options.putInt(OPTION_SYNC_SINGLE_TRANSACTION, t1.getId());
        options.putString(OPTION_SYNC_SINGLE_TRANSACTION_ACTION, SYNC_ACTION_ADJUST);
        subject.synchronize(account, options, syncResult);

        assertEquals(1, api.getAdjustTransactions().size());

        MockLedgerApi.AdjustPendingTransactionArgs adjusted = api.getAdjustTransactions().get(0);
        assertEquals(t1.transactionId, adjusted.transactionId);
        assertEquals(t1.amount, adjusted.amount);
        assertEquals(t1.comment, adjusted.comment);
        assertEquals(t1.accountId, adjusted.accountId);
    }

    public void testSynchronizeRejectDeleted() throws Exception {
        api.setPendingTransactions(new ArrayList<PendingTransactionDto>());

        PendingTransaction t1 = readModel.inject(new PendingTransaction("t-1", "100", "t 100", false, false, new Date(), null)
                .setId(112233).setAccountId("account-1").setTypeId(TRANSACTION_TYPE_EXPENSE));

        Bundle options = new Bundle();
        options.putInt(OPTION_SYNC_SINGLE_TRANSACTION, t1.getId());
        options.putString(OPTION_SYNC_SINGLE_TRANSACTION_ACTION, SYNC_ACTION_REJECT);
        subject.synchronize(account, options, syncResult);

        assertEquals(1, api.getRejectedPendingTrasnsactions().size());

        String rejectedId = api.getRejectedPendingTrasnsactions().get(0);
        assertEquals(rejectedId, t1.transactionId);
    }
}