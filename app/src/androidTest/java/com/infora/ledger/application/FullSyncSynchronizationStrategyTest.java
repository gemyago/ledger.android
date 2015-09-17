package com.infora.ledger.application;

import android.content.SyncResult;
import android.test.AndroidTestCase;

import com.infora.ledger.TransactionContract;
import com.infora.ledger.api.PendingTransactionDto;
import com.infora.ledger.application.commands.DeleteTransactionsCommand;
import com.infora.ledger.application.commands.MarkTransactionAsPublishedCommand;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.mocks.MockLedgerApi;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.mocks.MockTransactionsReadModel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import de.greenrobot.event.EventBus;

import static com.infora.ledger.TransactionContract.TRANSACTION_TYPE_EXPENSE;
import static com.infora.ledger.TransactionContract.TRANSACTION_TYPE_INCOME;

/**
 * Created by jenya on 25.03.15.
 */
public class FullSyncSynchronizationStrategyTest extends AndroidTestCase {

    private MockTransactionsReadModel readModel;
    private FullSyncSynchronizationStrategy subject;
    private MockLedgerApi api;
    private EventBus bus;
    private SyncResult syncResult;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        bus = new EventBus();
        readModel = new MockTransactionsReadModel();
        subject = new FullSyncSynchronizationStrategy(bus, readModel);
        api = new MockLedgerApi();
        syncResult = new SyncResult();
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
        subject.synchronize(api, null, syncResult);
        assertEquals(0, api.getReportedTransactions().size());
    }

    public void testSynchronizeReportNew() throws SQLException {
        api.setPendingTransactions(new ArrayList<PendingTransactionDto>());

        PendingTransaction t1 = readModel.inject(new PendingTransaction("t-1", "100", "t 100", false, false, new Date(), null).setId(1).setAccountId("account-1").setTypeId(TRANSACTION_TYPE_EXPENSE));
        PendingTransaction t2 = readModel.inject(new PendingTransaction("t-2", "101", "t 101", false, false, new Date(), null).setId(2));
        PendingTransaction t3 = readModel.inject(new PendingTransaction("t-3", "103", "t 103", false, false, new Date(), null).setId(3).setAccountId("account-3").setTypeId(TRANSACTION_TYPE_INCOME));
        MockSubscriber<MarkTransactionAsPublishedCommand> publishedSubscriber = new MockSubscriber<>(MarkTransactionAsPublishedCommand.class);
        bus.register(publishedSubscriber);

        subject.synchronize(api, null, syncResult);

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

    public void testSynchronizeRemoveLocallyRemoved() throws SQLException {
        ArrayList<PendingTransactionDto> remoteTransactions = new ArrayList<>();
        remoteTransactions.add(new PendingTransactionDto("t-1", "100", "t 100"));
        remoteTransactions.add(new PendingTransactionDto("t-2", "101", "t 101"));
        remoteTransactions.add(new PendingTransactionDto("t-3", "102", "t 102"));
        api.setPendingTransactions(remoteTransactions);

        subject.synchronize(api, null, syncResult);

        assertEquals(0, api.getReportedTransactions().size());

        assertEquals(3, api.getRejectedPendingTrasnsactions().size());
        assertTrue(api.getRejectedPendingTrasnsactions().contains("t-1"));
        assertTrue(api.getRejectedPendingTrasnsactions().contains("t-2"));
        assertTrue(api.getRejectedPendingTrasnsactions().contains("t-3"));
    }

    public void testSynchronizeUpdateChanged() throws SQLException {
        ArrayList<PendingTransactionDto> remoteTransactions = new ArrayList<>();
        remoteTransactions.add(new PendingTransactionDto("t-1", "100", "t 100"));
        remoteTransactions.add(new PendingTransactionDto("t-2", "101", "t 101"));
        remoteTransactions.add(new PendingTransactionDto("t-3", "103", "t 103"));
        api.setPendingTransactions(remoteTransactions);

        readModel.injectAnd(new PendingTransaction("t-1", "100.01", "t 100.01", true, false, null, null).setId(1).setAccountId("account-1"))
                .injectAnd(new PendingTransaction("t-2", "101", "t 101", true, false, null, null).setId(1))
                .inject(new PendingTransaction("t-3", "103.03", "t 103.03", true, false, null, null).setId(1).setAccountId("account-3"));

        subject.synchronize(api, null, syncResult);

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

    public void testSynchronizeDeleteApproved() throws SQLException {
        api.setPendingTransactions(new ArrayList<PendingTransactionDto>());
        readModel.inject(new PendingTransaction().setId(100).setTransactionId("t-100").setIsPublished(true));
        readModel.inject(new PendingTransaction().setId(101).setTransactionId("t-101").setIsPublished(true));
        readModel.inject(new PendingTransaction().setId(102).setTransactionId("t-102").setIsPublished(true));

        MockSubscriber<DeleteTransactionsCommand> deleteSubscriber = new MockSubscriber<>(DeleteTransactionsCommand.class);
        bus.register(deleteSubscriber);

        subject.synchronize(api, null, syncResult);

        assertEquals(1, deleteSubscriber.getEvents().size());
        assertEquals(3, deleteSubscriber.getEvent().ids.length);
        assertEquals(100, deleteSubscriber.getEvent().ids[0]);
        assertEquals(101, deleteSubscriber.getEvent().ids[1]);
        assertEquals(102, deleteSubscriber.getEvent().ids[2]);
    }
}