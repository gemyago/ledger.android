package com.infora.ledger.application;

import android.database.MatrixCursor;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.infora.ledger.TransactionContract;
import com.infora.ledger.api.PendingTransactionDto;
import com.infora.ledger.data.LedgerDbHelper;
import com.infora.ledger.mocks.MockLedgerApi;
import com.infora.ledger.mocks.MockPendingTransactionsContentProvider;
import com.infora.ledger.mocks.MockSubscriber;

import java.util.ArrayList;
import java.util.Date;

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
    }

    public void testSynchronizeIgnoreExisting() throws Exception {
        ArrayList<PendingTransactionDto> remoteTransactions = new ArrayList<>();
        remoteTransactions.add(new PendingTransactionDto("t-1", "100", "t 100"));
        remoteTransactions.add(new PendingTransactionDto("t-2", "101", "t 101"));
        remoteTransactions.add(new PendingTransactionDto("t-3", "102", "t 102"));
        api.setPendingTransactions(remoteTransactions);

        MatrixCursor matrixCursor = new MatrixCursor(TransactionContract.ALL_COLUMNS);
        for (PendingTransactionDto transaction : remoteTransactions) {
            matrixCursor.addRow(new Object[]{0, transaction.transactionId, transaction.amount, transaction.comment, 1, LedgerDbHelper.toISO8601(new Date())});
        }
        provider.setQueryResult(matrixCursor);

        subject.synchronize(api, resolver, null);

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
        Object[] t1 = {1, "t-1", "100", "t 100", 0, LedgerDbHelper.toISO8601(new Date())};
        matrixCursor.addRow(t1);
        Object[] t2 = {2, "t-2", "101", "t 101", 0, LedgerDbHelper.toISO8601(new Date())};
        matrixCursor.addRow(t2);
        Object[] t3 = {3, "t-3", "103", "t 103", 0, LedgerDbHelper.toISO8601(new Date())};
        matrixCursor.addRow(t3);
        provider.setQueryResult(matrixCursor);
        MockSubscriber<MarkTransactionAsPublishedCommand> publishedSubscriber = new MockSubscriber<>();
        bus.register(publishedSubscriber);

        subject.synchronize(api, resolver, null);

        assertNull(provider.getInsertArgs());
        assertNull(provider.getDeleteArgs());
        assertNull(provider.getUpdateArgs());

        assertEquals(3, api.getReportedTransactions().size());

        MockLedgerApi.ReportPendingTransactionArgs reported1 = api.getReportedTransactions().get(0);
        assertEquals(t1[1], reported1.getTransactionId());
        assertEquals(t1[2], reported1.getAmount());
        assertEquals(t1[3], reported1.getComment());
        assertEquals(LedgerDbHelper.parseISO8601((String) t1[5]), reported1.getDate());

        MockLedgerApi.ReportPendingTransactionArgs reported2 = api.getReportedTransactions().get(1);
        assertEquals(t2[1], reported2.getTransactionId());
        assertEquals(t2[2], reported2.getAmount());
        assertEquals(t2[3], reported2.getComment());
        assertEquals(LedgerDbHelper.parseISO8601((String) t2[5]), reported2.getDate());

        MockLedgerApi.ReportPendingTransactionArgs reported3 = api.getReportedTransactions().get(2);
        assertEquals(t3[1], reported3.getTransactionId());
        assertEquals(t3[2], reported3.getAmount());
        assertEquals(t3[3], reported3.getComment());
        assertEquals(LedgerDbHelper.parseISO8601((String) t3[5]), reported3.getDate());

        assertEquals(3, publishedSubscriber.getEvents().size());
        assertEquals(1, publishedSubscriber.getEvents().get(0).getId());
        assertEquals(2, publishedSubscriber.getEvents().get(1).getId());
        assertEquals(3, publishedSubscriber.getEvents().get(2).getId());
    }

    public void testSynchronizePurgePublished() {
        api.setPendingTransactions(new ArrayList<PendingTransactionDto>());

        MatrixCursor matrixCursor = new MatrixCursor(TransactionContract.ALL_COLUMNS);
        Object[] t1 = {1, "t-1", "100", "t 100", 1, LedgerDbHelper.toISO8601(new Date())};
        matrixCursor.addRow(t1);
        Object[] t2 = {2, "t-2", "101", "t 101", 1, LedgerDbHelper.toISO8601(new Date())};
        matrixCursor.addRow(t2);
        Object[] t3 = {3, "t-3", "103", "t 103", 1, LedgerDbHelper.toISO8601(new Date())};
        matrixCursor.addRow(t3);
        provider.setQueryResult(matrixCursor);
        MockSubscriber<PurgeTransactionsCommand> publishedSubscriber = new MockSubscriber<>();
        bus.register(publishedSubscriber);

        subject.synchronize(api, resolver, null);

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
}