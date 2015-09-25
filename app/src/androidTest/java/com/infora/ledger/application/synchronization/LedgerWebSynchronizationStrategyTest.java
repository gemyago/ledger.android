package com.infora.ledger.application.synchronization;

import android.accounts.Account;
import android.content.SyncResult;
import android.os.Bundle;
import android.test.AndroidTestCase;

import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.api.PendingTransactionDto;
import com.infora.ledger.application.commands.DeleteTransactionsCommand;
import com.infora.ledger.application.commands.MarkTransactionAsPublishedCommand;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.mocks.MockDatabaseContext;
import com.infora.ledger.mocks.MockLedgerApi;
import com.infora.ledger.mocks.MockLedgerApiFactory;
import com.infora.ledger.mocks.MockPendingTransactionsService;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.mocks.MockTransactionsReadModel;

import java.util.ArrayList;
import java.util.Date;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;

import static com.infora.ledger.TransactionContract.TRANSACTION_TYPE_EXPENSE;
import static com.infora.ledger.TransactionContract.TRANSACTION_TYPE_INCOME;

/**
 * Created by jenya on 25.03.15.
 */
public class LedgerWebSynchronizationStrategyTest extends AndroidTestCase {

    private MockTransactionsReadModel readModel;
    private LedgerWebSynchronizationStrategy subject;
    private MockLedgerApi api;
    private EventBus bus;
    private SyncResult syncResult;
    private Account account;
    private MockLedgerApiFactory apiFactory;
    private MockPendingTransactionsService pendingTransactionsService;

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
        pendingTransactionsService = new MockPendingTransactionsService();
        subject = new LedgerWebSynchronizationStrategy(bus, pendingTransactionsService, readModel, apiFactory);
        syncResult = new SyncResult();
    }

    public void testSynchronizeAuthError() throws Exception {
        final SyncResult testSyncResult = new SyncResult();
        final Bundle extras = new Bundle();
        apiFactory.onCreatingApi = new MockLedgerApiFactory.OnCreatingApi() {
            @Override
            public LedgerApi call(Account account) {
                Response response = new Response("localhost", 401, "Unauthorized", new ArrayList<Header>(), null);
                throw RetrofitError.httpError("test", response, null, null);
            }
        };

        boolean thrown = false;
        try {
            subject.synchronize(account, extras, testSyncResult);
        } catch (RetrofitError error) {
            thrown = true;
        }
        assertTrue(thrown);
        assertEquals(1, testSyncResult.stats.numAuthExceptions);
    }

    public void testSynchronizeIgnoreExisting() throws Exception {
        ArrayList<PendingTransactionDto> remoteTransactions = new ArrayList<>();
        remoteTransactions.add(new PendingTransactionDto("t-1", "100", "t 100"));
        remoteTransactions.add(new PendingTransactionDto("t-2", "101", "t 101"));
        remoteTransactions.add(new PendingTransactionDto("t-3", "102", "t 102"));
        api.setPendingTransactions(remoteTransactions);
        for (PendingTransactionDto dto : remoteTransactions) {
            readModel.inject(dto.toTransaction());
        }
        subject.synchronize(account, null, syncResult);
        assertEquals(0, api.getReportedTransactions().size());
    }

    public void testSynchronizeReportNew() throws Exception {
        api.setPendingTransactions(new ArrayList<PendingTransactionDto>());

        PendingTransaction t1 = readModel.inject(new PendingTransaction("t-1", "100", "t 100", false, false, new Date(), null).setId(1).setAccountId("account-1").setTypeId(TRANSACTION_TYPE_EXPENSE));
        PendingTransaction t2 = readModel.inject(new PendingTransaction("t-2", "101", "t 101", false, false, new Date(), null).setId(2));
        PendingTransaction t3 = readModel.inject(new PendingTransaction("t-3", "103", "t 103", false, false, new Date(), null).setId(3).setAccountId("account-3").setTypeId(TRANSACTION_TYPE_INCOME));
        MockSubscriber<MarkTransactionAsPublishedCommand> publishedSubscriber = new MockSubscriber<>(MarkTransactionAsPublishedCommand.class);
        bus.register(publishedSubscriber);

        subject.synchronize(account, null, syncResult);

        assertEquals(3, api.getReportedTransactions().size());

        PendingTransaction reported1 = api.getReportedTransactions().get(0).toTransaction().setId(t1.id);
        assertEquals(t1, reported1);

        PendingTransaction reported2 = api.getReportedTransactions().get(1).toTransaction().setId(t2.id);
        assertEquals(t2, reported2);

        PendingTransaction reported3 = api.getReportedTransactions().get(2).toTransaction().setId(t3.id);
        assertEquals(t3, reported3);

        assertEquals(3, publishedSubscriber.getEvents().size());
        assertEquals(1, publishedSubscriber.getEvents().get(0).id);
        assertEquals(2, publishedSubscriber.getEvents().get(1).id);
        assertEquals(3, publishedSubscriber.getEvents().get(2).id);
    }

    public void testSynchronizeRemoveLocallyRemoved() throws Exception {
        ArrayList<PendingTransactionDto> remoteTransactions = new ArrayList<>();
        remoteTransactions.add(new PendingTransactionDto("t-1", "100", "t 100"));
        remoteTransactions.add(new PendingTransactionDto("t-2", "101", "t 101"));
        remoteTransactions.add(new PendingTransactionDto("t-3", "102", "t 102"));
        api.setPendingTransactions(remoteTransactions);

        subject.synchronize(account, null, syncResult);

        assertEquals(0, api.getReportedTransactions().size());

        assertEquals(3, api.getRejectedPendingTrasnsactions().size());
        assertTrue(api.getRejectedPendingTrasnsactions().contains("t-1"));
        assertTrue(api.getRejectedPendingTrasnsactions().contains("t-2"));
        assertTrue(api.getRejectedPendingTrasnsactions().contains("t-3"));
    }

    public void testSynchronizeUpdateChanged() throws Exception {
        ArrayList<PendingTransactionDto> remoteTransactions = new ArrayList<>();
        remoteTransactions.add(new PendingTransactionDto("t-1", "100", "t 100"));
        remoteTransactions.add(new PendingTransactionDto("t-2", "101", "t 101"));
        remoteTransactions.add(new PendingTransactionDto("t-3", "103", "t 103"));
        api.setPendingTransactions(remoteTransactions);

        readModel.injectAnd(new PendingTransaction("t-1", "100.01", "t 100.01", true, false, null, null).setId(1).setAccountId("account-1"))
                .injectAnd(new PendingTransaction("t-2", "101", "t 101", true, false, null, null).setId(1))
                .inject(new PendingTransaction("t-3", "103.03", "t 103.03", true, false, null, null).setId(1).setAccountId("account-3"));

        subject.synchronize(account, null, syncResult);

        assertEquals(0, api.getReportedTransactions().size());
        assertEquals(2, api.getAdjustTransactions().size());
        assertEquals(0, api.getRejectedPendingTrasnsactions().size());

        MockLedgerApi.AdjustPendingTransactionArgs adjustedT1 = api.getAdjustTransactions().get(0);
        assertEquals("t-1", adjustedT1.transactionId);
        assertEquals("100.01", adjustedT1.amount);
        assertEquals("t 100.01", adjustedT1.comment);
        assertEquals("account-1", adjustedT1.accountId);

        MockLedgerApi.AdjustPendingTransactionArgs adjustedT3 = api.getAdjustTransactions().get(1);
        assertEquals("t-3", adjustedT3.transactionId);
        assertEquals("103.03", adjustedT3.amount);
        assertEquals("t 103.03", adjustedT3.comment);
        assertEquals("account-3", adjustedT3.accountId);
    }

    public void testSynchronizeDeleteApproved() throws Exception {
        api.setPendingTransactions(new ArrayList<PendingTransactionDto>());
        readModel.inject(new PendingTransaction().setId(100).setTransactionId("t-100").setIsPublished(true));
        readModel.inject(new PendingTransaction().setId(101).setTransactionId("t-101").setIsPublished(true));
        readModel.inject(new PendingTransaction().setId(102).setTransactionId("t-102").setIsPublished(true));

        subject.synchronize(account, null, syncResult);

        assertEquals(1, pendingTransactionsService.deleteCommands.size());
        DeleteTransactionsCommand deleteCommand = pendingTransactionsService.deleteCommands.get(0);

        assertEquals(3, deleteCommand.ids.length);
        assertEquals(100, deleteCommand.ids[0]);
        assertEquals(101, deleteCommand.ids[1]);
        assertEquals(102, deleteCommand.ids[2]);
    }
}