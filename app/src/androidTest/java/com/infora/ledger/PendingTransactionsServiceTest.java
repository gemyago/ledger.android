package com.infora.ledger;

import android.content.ContentUris;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.infora.ledger.application.DeleteTransactionsCommand;
import com.infora.ledger.application.MarkTransactionAsPublishedCommand;
import com.infora.ledger.application.PendingTransactionsService;
import com.infora.ledger.application.PurgeTransactionsCommand;
import com.infora.ledger.application.ReportTransactionCommand;
import com.infora.ledger.application.TransactionsDeletedEvent;
import com.infora.ledger.application.TransactionReportedEvent;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.mocks.MockPendingTransactionsContentProvider;

import java.util.ArrayList;

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
        super(MockPendingTransactionsContentProvider.class, TransactionContract.AUTHORITY);
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
        assertEquals(TransactionContract.CONTENT_URI, provider.getInsertArgs().getUri());
        assertEquals(2, provider.getInsertArgs().getValues().size());
        assertEquals("100.01", provider.getInsertArgs().getValues().getAsString(TransactionContract.COLUMN_AMOUNT));
        assertEquals("Comment 100.01", provider.getInsertArgs().getValues().getAsString(TransactionContract.COLUMN_COMMENT));
    }

    public void testReportPendingTransactionRaisesReportedEvent() {
        MockSubscriber<TransactionReportedEvent> subscriber = new MockSubscriber<>();
        bus.register(subscriber);
        provider.setInsertedUri(ContentUris.withAppendedId(TransactionContract.CONTENT_URI, 100));
        subject.onEventBackgroundThread(new ReportTransactionCommand("100.01", "Comment 100.01"));
        assertEquals(100, subscriber.getEvent().getId());
    }

    public void testMarkTransactionAsPublished() {
        subject.onEvent(new MarkTransactionAsPublishedCommand(3321L));
        MockPendingTransactionsContentProvider.UpdateArgs updateArgs = provider.getUpdateArgs();
        assertNotNull(provider.getUpdateArgs());
        assertEquals(ContentUris.withAppendedId(TransactionContract.CONTENT_URI, 3321L), updateArgs.uri);
        assertEquals(true, (boolean) updateArgs.values.getAsBoolean(TransactionContract.COLUMN_IS_PUBLISHED));
    }

    public void testDeleteTransactions() {
        MockSubscriber<TransactionsDeletedEvent> deletedSubscriber = new MockSubscriber<>();
        bus.register(deletedSubscriber);

        DeleteTransactionsCommand command = new DeleteTransactionsCommand(3321L, 3322L, 3323L);
        subject.onEventBackgroundThread(command);
        ArrayList<MockPendingTransactionsContentProvider.UpdateArgs> allUpdateArgs = provider.getAllUpdateArgs();
        assertEquals(3, allUpdateArgs.size());

        for (int i = 0; i < allUpdateArgs.size(); i++) {
            MockPendingTransactionsContentProvider.UpdateArgs updateArgs = allUpdateArgs.get(i);
            assertEquals(ContentUris.withAppendedId(TransactionContract.CONTENT_URI, 3321L + i), updateArgs.uri);
            assertEquals(true, (boolean) updateArgs.values.getAsBoolean(TransactionContract.COLUMN_IS_DELETED));
        }
        assertEquals(1, deletedSubscriber.getEvents().size());
        assertEquals(command.getIds(), deletedSubscriber.getEvent().getIds());
    }

    public void testPurgeTransactions() {
        subject.onEventBackgroundThread(new PurgeTransactionsCommand(3321L));
        assertEquals(ContentUris.withAppendedId(TransactionContract.CONTENT_URI, 3321L), provider.getDeleteArgs().getUri());
    }
}
