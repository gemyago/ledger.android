package com.infora.ledger.data;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.infora.ledger.DbUtils;
import com.infora.ledger.support.ObjectNotFoundException;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by jenya on 16.06.15.
 */
public class TransactionsReadModelTest extends AndroidTestCase {

    private TransactionsReadModel subject;
    private DatabaseRepository<PendingTransaction> repo;

    @Override
    protected void setUp() throws Exception {
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "-transactions-read-model-test");
        DbUtils.deleteAllDatabases(context);
        repo = new DatabaseRepository<>(PendingTransaction.class, context);
        subject = new TransactionsReadModel(context);
        super.setUp();
    }

    public void testGetById() throws SQLException {
        PendingTransaction t1 = new PendingTransaction().setAmount("100")
                .setBic("bic-1").setTransactionId("t-1").setTimestamp(new Date()).save(repo);
        PendingTransaction t2 = new PendingTransaction().setAmount("100")
                .setBic("bic-1").setTransactionId("t-2").setTimestamp(new Date()).save(repo);

        assertEquals(t1, subject.getById(t1.id));
        assertEquals(t2, subject.getById(t2.id));
    }

    public void testGetByIdNotExisting() throws SQLException {
        PendingTransaction t1 = new PendingTransaction().setAmount("100")
                .setBic("bic-1").setTransactionId("t-1").setTimestamp(new Date()).save(repo);
        repo.deleteAll(new long[] { t1.id });
        boolean raised = false;
        try {
            subject.getById(t1.id);
        } catch (ObjectNotFoundException ex) {
            raised = true;
        }
        assertTrue(raised);
    }

    public void testGetTransactions() throws SQLException {
        PendingTransaction t1 = new PendingTransaction().setAmount("100")
                .setBic("bic-1").setTransactionId("t-1").setTimestamp(new Date()).save(repo);
        PendingTransaction t2 = new PendingTransaction().setAmount("100")
                .setBic("bic-1").setTransactionId("t-2").setTimestamp(new Date()).save(repo);
        new PendingTransaction().setAmount("100").setIsDeleted(true)
                .setBic("bic-1").setTransactionId("t-3").setTimestamp(new Date()).save(repo);
        PendingTransaction t4 = new PendingTransaction().setAmount("100")
                .setBic("bic-1").setTransactionId("t-4").setTimestamp(new Date()).save(repo);

        List<PendingTransaction> transactions = subject.getTransactions();
        assertEquals(3, transactions.size());
        assertTrue(transactions.contains(t1));
        assertTrue(transactions.contains(t2));
        assertTrue(transactions.contains(t4));
    }

    public void testIsTransactionExists() throws SQLException {
        PendingTransaction t1 = new PendingTransaction().setAmount("100")
                .setBic("bic-1").setTransactionId("t-1").setTimestamp(new Date()).save(repo);
        PendingTransaction t2 = new PendingTransaction().setAmount("100")
                .setIsDeleted(true)
                .setBic("bic-1").setTransactionId("t-2").setTimestamp(new Date()).save(repo);

        assertTrue(subject.isTransactionExists(t1.transactionId));
        assertTrue(subject.isTransactionExists(t2.transactionId));

        repo.deleteAll(new long[]{t1.id, t2.id});

        assertFalse(subject.isTransactionExists(t1.transactionId));
        assertFalse(subject.isTransactionExists(t2.transactionId));
    }
}