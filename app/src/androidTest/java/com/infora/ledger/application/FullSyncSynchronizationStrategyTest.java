package com.infora.ledger.application;

import android.content.SyncResult;
import android.database.MatrixCursor;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.infora.ledger.DbUtils;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.TransactionContract;
import com.infora.ledger.api.PendingTransactionDto;
import com.infora.ledger.application.commands.MarkTransactionAsPublishedCommand;
import com.infora.ledger.application.commands.PurgeTransactionsCommand;
import com.infora.ledger.data.LedgerDbHelper;
import com.infora.ledger.mocks.MockLedgerApi;
import com.infora.ledger.mocks.MockPendingTransactionsContentProvider;
import com.infora.ledger.mocks.MockSubscriber;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 25.03.15.
 */
public class FullSyncSynchronizationStrategyTest extends ProviderTestCase2<MockPendingTransactionsContentProvider> {

    private MockPendingTransactionsContentProvider provider;
    private MockContentResolver resolver;
    private FullSyncSynchronizationStrategy subject;
    private MockLedgerApi api;
    private EventBus bus;
    private SyncResult syncResult;

    public FullSyncSynchronizationStrategyTest() {
        super(MockPendingTransactionsContentProvider.class, TransactionContract.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        resolver = getMockContentResolver();
        provider = getProvider();
        bus = new EventBus();
        subject = new FullSyncSynchronizationStrategy(bus);
        api = new MockLedgerApi();
        syncResult = new SyncResult();
    }

    public void testSynchronizeIgnoreExisting() throws Exception {
        ArrayList<PendingTransactionDto> remoteTransactions = new ArrayList<>();
        remoteTransactions.add(new PendingTransactionDto("t-1", "100", "t 100"));
        remoteTransactions.add(new PendingTransactionDto("t-2", "101", "t 101"));
        remoteTransactions.add(new PendingTransactionDto("t-3", "102", "t 102"));
        api.setPendingTransactions(remoteTransactions);

        MatrixCursor matrixCursor = new MatrixCursor(TransactionContract.ALL_COLUMNS);
        for (PendingTransactionDto dto : remoteTransactions) {
            matrixCursor.addRow(DbUtils.toArray(dto.toTransaction()));
        }
        provider.setQueryResult(matrixCursor);

        subject.synchronize(api, resolver, null, syncResult);

        assertNull(provider.getInsertArgs());
        assertNull(provider.getDeleteArgs());
        assertNull(provider.getUpdateArgs());
        assertNotNull("The provider was not queried", provider.getQueryArgs());
        assertEquals(TransactionContract.CONTENT_URI, provider.getQueryArgs().uri);
        assertEquals(0, api.getReportedTransactions().size());
    }

    public void testSynchronizeReportNew() {
        api.setPendingTransactions(new ArrayList<PendingTransactionDto>());

        MatrixCursor matrixCursor = new MatrixCursor(TransactionContract.ALL_COLUMNS);
        Object[] t1 = DbUtils.toArray(new PendingTransaction("t-1", "100", "t 100", false, false, null, null).setId(1));
        matrixCursor.addRow(t1);
        Object[] t2 = DbUtils.toArray(new PendingTransaction("t-2", "101", "t 101", false, false, null, null).setId(2));
        matrixCursor.addRow(t2);
        Object[] t3 = DbUtils.toArray(new PendingTransaction("t-3", "103", "t 103", false, false, null, null).setId(3));
        matrixCursor.addRow(t3);
        provider.setQueryResult(matrixCursor);
        MockSubscriber<MarkTransactionAsPublishedCommand> publishedSubscriber = new MockSubscriber<>(MarkTransactionAsPublishedCommand.class);
        bus.register(publishedSubscriber);

        subject.synchronize(api, resolver, null, syncResult);

        assertNull(provider.getInsertArgs());
        assertNull(provider.getDeleteArgs());
        assertNull(provider.getUpdateArgs());

        assertEquals(3, api.getReportedTransactions().size());

        MockLedgerApi.ReportPendingTransactionArgs reported1 = api.getReportedTransactions().get(0);
        assertEquals(t1[2], reported1.getTransactionId());
        assertEquals(t1[3], reported1.getAmount());
        assertEquals(t1[4], reported1.getComment());
        assertEquals(LedgerDbHelper.parseISO8601((String) t1[7]), reported1.getDate());

        MockLedgerApi.ReportPendingTransactionArgs reported2 = api.getReportedTransactions().get(1);
        assertEquals(t2[2], reported2.getTransactionId());
        assertEquals(t2[3], reported2.getAmount());
        assertEquals(t2[4], reported2.getComment());
        assertEquals(LedgerDbHelper.parseISO8601((String) t2[7]), reported2.getDate());

        MockLedgerApi.ReportPendingTransactionArgs reported3 = api.getReportedTransactions().get(2);
        assertEquals(t3[2], reported3.getTransactionId());
        assertEquals(t3[3], reported3.getAmount());
        assertEquals(t3[4], reported3.getComment());
        assertEquals(LedgerDbHelper.parseISO8601((String) t3[7]), reported3.getDate());

        assertEquals(3, publishedSubscriber.getEvents().size());
        assertEquals(1, publishedSubscriber.getEvents().get(0).getId());
        assertEquals(2, publishedSubscriber.getEvents().get(1).getId());
        assertEquals(3, publishedSubscriber.getEvents().get(2).getId());
    }

    public void testSynchronizePurgePublished() {
        api.setPendingTransactions(new ArrayList<PendingTransactionDto>());

        MatrixCursor matrixCursor = new MatrixCursor(TransactionContract.ALL_COLUMNS);
        Object[] t1 = DbUtils.toArray(new PendingTransaction("t-1", "100", "t 100", true, false, null, null).setId(1));
        matrixCursor.addRow(t1);
        Object[] t2 = DbUtils.toArray(new PendingTransaction("t-2", "101", "t 101", true, false, null, null).setId(2));
        matrixCursor.addRow(t2);
        Object[] t3 = DbUtils.toArray(new PendingTransaction("t-3", "103", "t 103", true, false, null, null).setId(3));
        matrixCursor.addRow(t3);
        provider.setQueryResult(matrixCursor);
        MockSubscriber<PurgeTransactionsCommand> publishedSubscriber = new MockSubscriber<>(PurgeTransactionsCommand.class);
        bus.register(publishedSubscriber);

        subject.synchronize(api, resolver, null, syncResult);

        assertNull(provider.getInsertArgs());
        assertNull(provider.getDeleteArgs());
        assertNull(provider.getUpdateArgs());

        assertEquals(0, api.getReportedTransactions().size());

        assertEquals(1, publishedSubscriber.getEvents().size());
        long[] removedIds = publishedSubscriber.getEvent().getIds();
        assertEquals(3, removedIds.length);
        assertEquals(1, removedIds[0]);
        assertEquals(2, removedIds[1]);
        assertEquals(3, removedIds[2]);
    }

    public void testSynchronizeRemoveAndPurgeLocallyRemoved() {
        ArrayList<PendingTransactionDto> remoteTransactions = new ArrayList<>();
        remoteTransactions.add(new PendingTransactionDto("t-1", "100", "t 100"));
        remoteTransactions.add(new PendingTransactionDto("t-2", "101", "t 101"));
        remoteTransactions.add(new PendingTransactionDto("t-3", "102", "t 102"));
        api.setPendingTransactions(remoteTransactions);


        MatrixCursor matrixCursor = new MatrixCursor(TransactionContract.ALL_COLUMNS);
        Object[] t1 = DbUtils.toArray(new PendingTransaction("t-1", "100", "t 100", true, true, null, null).setId(1));
        matrixCursor.addRow(t1);
        Object[] t2 = DbUtils.toArray(new PendingTransaction("t-2", "101", "t 101", true, true, null, null).setId(2));
        matrixCursor.addRow(t2);
        Object[] t3 = DbUtils.toArray(new PendingTransaction("t-3", "103", "t 103", true, true, null, null).setId(3));
        matrixCursor.addRow(t3);
        provider.setQueryResult(matrixCursor);

        MockSubscriber<PurgeTransactionsCommand> purgedSubscriber = new MockSubscriber<>(PurgeTransactionsCommand.class);
        bus.register(purgedSubscriber);

        subject.synchronize(api, resolver, null, syncResult);

        assertNull(provider.getInsertArgs());
        assertNull(provider.getDeleteArgs());
        assertNull(provider.getUpdateArgs());

        assertEquals(0, api.getReportedTransactions().size());

        assertEquals(1, purgedSubscriber.getEvents().size());
        long[] removedIds = purgedSubscriber.getEvent().getIds();
        assertEquals(3, removedIds.length);
        assertEquals(1, removedIds[0]);
        assertEquals(2, removedIds[1]);
        assertEquals(3, removedIds[2]);

        assertEquals(3, api.getRejectedPendingTrasnsactions().size());
        assertTrue(api.getRejectedPendingTrasnsactions().contains("t-1"));
        assertTrue(api.getRejectedPendingTrasnsactions().contains("t-2"));
        assertTrue(api.getRejectedPendingTrasnsactions().contains("t-3"));
    }

    public void testSynchronizeUpdateChanged() {
        ArrayList<PendingTransactionDto> remoteTransactions = new ArrayList<>();
        remoteTransactions.add(new PendingTransactionDto("t-1", "100", "t 100"));
        remoteTransactions.add(new PendingTransactionDto("t-2", "101", "t 101"));
        remoteTransactions.add(new PendingTransactionDto("t-3", "103", "t 103"));
        api.setPendingTransactions(remoteTransactions);


        MatrixCursor matrixCursor = new MatrixCursor(TransactionContract.ALL_COLUMNS);
        Object[] t1 = DbUtils.toArray(new PendingTransaction("t-1", "100.01", "t 100.01", true, false, null, null).setId(1));
        matrixCursor.addRow(t1);
        Object[] t2 = DbUtils.toArray(new PendingTransaction("t-2", "101", "t 101", true, false, null, null).setId(1));
        matrixCursor.addRow(t2);
        Object[] t3 = DbUtils.toArray(new PendingTransaction("t-3", "103.03", "t 103.03", true, false, null, null).setId(1));
        matrixCursor.addRow(t3);
        provider.setQueryResult(matrixCursor);

        subject.synchronize(api, resolver, null, syncResult);

        assertNull(provider.getInsertArgs());
        assertNull(provider.getDeleteArgs());
        assertNull(provider.getUpdateArgs());

        assertEquals(0, api.getReportedTransactions().size());
        assertEquals(2, api.getAdjustTransactions().size());
        assertEquals(0, api.getRejectedPendingTrasnsactions().size());

        MockLedgerApi.AdjustPendingTransactionArgs adjustedT1 = api.getAdjustTransactions().get(0);
        assertEquals("t-1", adjustedT1.transactionId);
        assertEquals("100.01", adjustedT1.amount);
        assertEquals("t 100.01", adjustedT1.comment);

        MockLedgerApi.AdjustPendingTransactionArgs adjustedT3 = api.getAdjustTransactions().get(1);
        assertEquals("t-3", adjustedT3.transactionId);
        assertEquals("103.03", adjustedT3.amount);
        assertEquals("t 103.03", adjustedT3.comment);
    }
}