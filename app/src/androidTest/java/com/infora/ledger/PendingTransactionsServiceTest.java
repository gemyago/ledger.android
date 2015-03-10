package com.infora.ledger;

import android.content.ContentUris;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.infora.ledger.application.PendingTransactionsService;
import com.infora.ledger.application.ReportTransactionCommand;
import com.infora.ledger.application.TransactionReportedEvent;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.mocks.MockPendingTransactionsContentProvider;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 10.03.15.
 */
public class PendingTransactionsServiceTest extends ProviderTestCase2<MockPendingTransactionsContentProvider> {

    private MockContentResolver resolver;
    private MockPendingTransactionsContentProvider provider;
    private PendingTransactionsService subject;
    private EventBus bus;

    public PendingTransactionsServiceTest() {
        super(MockPendingTransactionsContentProvider.class, PendingTransactionContract.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        resolver = getMockContentResolver();
        provider = getProvider();
        bus = new EventBus();
        subject = new PendingTransactionsService(resolver, bus);
    }

    public void testReportPendingTransaction() {
        subject.onEventBackgroundThread(new ReportTransactionCommand("100.01", "Comment 100.01"));
        assertEquals(PendingTransactionContract.CONTENT_URI, provider.getInsertArgs().getUri());
        assertEquals(2, provider.getInsertArgs().getValues().size());
        assertEquals("100.01", provider.getInsertArgs().getValues().getAsString(PendingTransactionContract.COLUMN_AMOUNT));
        assertEquals("Comment 100.01", provider.getInsertArgs().getValues().getAsString(PendingTransactionContract.COLUMN_COMMENT));
    }

    public void testReportPendingTransactionRaisesReportedEvent() {
        MockSubscriber<TransactionReportedEvent> subscriber = new MockSubscriber<>();
        bus.register(subscriber);
        provider.setInsertedUri(ContentUris.withAppendedId(PendingTransactionContract.CONTENT_URI, 100));
        subject.onEventBackgroundThread(new ReportTransactionCommand("100.01", "Comment 100.01"));
        assertEquals(100, subscriber.getEvent().getId());
    }
}
