package com.infora.ledger;

import android.test.ProviderTestCase2;

import com.infora.ledger.application.PendingTransactionsService;
import com.infora.ledger.application.commands.AdjustTransactionCommand;
import com.infora.ledger.application.commands.DeleteTransactionsCommand;
import com.infora.ledger.application.commands.MarkTransactionAsPublishedCommand;
import com.infora.ledger.application.commands.ReportTransactionCommand;
import com.infora.ledger.application.events.TransactionAdjusted;
import com.infora.ledger.application.events.TransactionReportedEvent;
import com.infora.ledger.application.events.TransactionsDeletedEvent;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.mocks.MockDatabaseContext;
import com.infora.ledger.mocks.MockDatabaseRepository;
import com.infora.ledger.mocks.MockPendingTransactionsContentProvider;
import com.infora.ledger.mocks.MockSubscriber;

import java.sql.SQLException;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 10.03.15.
 */
public class PendingTransactionsServiceTest extends ProviderTestCase2<MockPendingTransactionsContentProvider> {
    private PendingTransactionsService subject;
    private MockDatabaseRepository<PendingTransaction> repo;
    private EventBus bus;

    public PendingTransactionsServiceTest() {
        super(MockPendingTransactionsContentProvider.class, TransactionContract.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        bus = new EventBus();
        MockDatabaseContext db = new MockDatabaseContext();
        repo = new MockDatabaseRepository<>(PendingTransaction.class);
        db.addMockRepo(PendingTransaction.class, repo);
        subject = new PendingTransactionsService(db, bus);
    }

    public void testOnReportTransactionCommand() throws SQLException {
        final PendingTransaction transaction = new PendingTransaction().setId(100);
        final ReportTransactionCommand command = new ReportTransactionCommand("account-100", "100.01", "Comment 100.01");
        MockSubscriber<TransactionReportedEvent> subscriber = new MockSubscriber<>(TransactionReportedEvent.class);
        bus.register(subscriber);
        subject = new PendingTransactionsService(new MockDatabaseContext(), bus) {
            @Override
            public PendingTransaction reportPendingTransaction(ReportTransactionCommand c) throws SQLException {
                assertSame(command, c);
                return transaction;
            }
        };
        subject.onEventBackgroundThread(command);
        assertEquals(100, subscriber.getEvent().getId());
    }

    public void testReportPendingTransaction() throws SQLException {
        PendingTransaction reportedT = subject.reportPendingTransaction(new ReportTransactionCommand("account-100", "100.01", "Comment 100.01"));
        assertEquals(1, repo.savedEntities.size());
        PendingTransaction transaction = repo.savedEntities.get(0);
        assertSame(reportedT, transaction);
        assertNotNull(transaction.transactionId);
        assertNotNull(transaction.timestamp);
        assertEquals(TransactionContract.TRANSACTION_TYPE_EXPENSE, transaction.typeId);
        assertEquals("account-100", transaction.accountId);
        assertEquals("100.01", transaction.amount);
        assertEquals("Comment 100.01", transaction.comment);
    }

    public void testAdjustTransactionCommand() throws SQLException {
        PendingTransaction transaction = new PendingTransaction().setId(10311);
        repo.entitiesToGetById.add(transaction);
        MockSubscriber<TransactionAdjusted> adjustedSubscriber = new MockSubscriber<>(TransactionAdjusted.class);
        bus.register(adjustedSubscriber);
        subject.onEventBackgroundThread(new AdjustTransactionCommand(10311, "100.01", "Comment 100.01"));
        assertEquals(1, repo.savedEntities.size());
        assertTrue(repo.savedEntities.contains(transaction));
        assertEquals("100.01", transaction.amount);
        assertEquals("Comment 100.01", transaction.comment);
        assertEquals(1, adjustedSubscriber.getEvents().size());
        assertEquals(transaction.id, adjustedSubscriber.getEvent().id);
    }

    public void testMarkTransactionAsPublished() throws SQLException {
        PendingTransaction transaction = new PendingTransaction().setId(3321);
        repo.entitiesToGetById.add(transaction);
        subject.onEvent(new MarkTransactionAsPublishedCommand(3321L));
        assertEquals(1, repo.savedEntities.size());
        assertTrue(repo.savedEntities.contains(transaction));
        assertTrue(transaction.isPublished);
    }

    public void testDeleteTransactions() throws SQLException {
        PendingTransaction t1 = new PendingTransaction().setId(3321);
        PendingTransaction t2 = new PendingTransaction().setId(3322);
        PendingTransaction t3 = new PendingTransaction().setId(3323);
        repo.entitiesToGetById.add(t1);
        repo.entitiesToGetById.add(t2);
        repo.entitiesToGetById.add(t3);

        DeleteTransactionsCommand command = new DeleteTransactionsCommand(3321L, 3322L, 3323L);
        subject.deleteTransactions(command);

        assertEquals(3, repo.savedEntities.size());
        assertTrue(repo.savedEntities.contains(t1));
        assertTrue(repo.savedEntities.contains(t2));
        assertTrue(repo.savedEntities.contains(t3));

        assertTrue(t1.isDeleted);
        assertTrue(t2.isDeleted);
        assertTrue(t3.isDeleted);
    }

    public void testOnDeleteTransactions() throws SQLException {
        MockSubscriber<TransactionsDeletedEvent> deletedSubscriber = new MockSubscriber<>(TransactionsDeletedEvent.class);
        bus.register(deletedSubscriber);
        final DeleteTransactionsCommand command = new DeleteTransactionsCommand(3321L, 3322L, 3323L);
        subject = new PendingTransactionsService(new MockDatabaseContext(), bus) {
            @Override
            public void deleteTransactions(DeleteTransactionsCommand c) throws SQLException {
                assertSame(command, c);
            }
        };
        subject.onEventBackgroundThread(command);
        assertEquals(1, deletedSubscriber.getEvents().size());
        assertEquals(command.ids, deletedSubscriber.getEvent().getIds());
    }
}
