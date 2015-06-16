package com.infora.ledger.data;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.infora.ledger.DbUtils;
import com.infora.ledger.TestHelper;
import com.infora.ledger.support.Dates;

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

    public void testGetTransactionsFetchedFromBank() throws Exception {
        insertSeedTransactions();
        Date startDate = Dates.startOfDay(TestHelper.randomDate());
        Date endDate = Dates.addDays(startDate, 10);
        new PendingTransaction().setAmount("100")
                .setBic("bic-1").setTransactionId("t-90").setTimestamp(Dates.addDays(startDate, -2)).save(repo);
        new PendingTransaction().setAmount("100")
                .setBic("bic-1").setTransactionId("t-91").setTimestamp(Dates.addDays(startDate, -1)).save(repo);
        PendingTransaction t100 = new PendingTransaction().setAmount("100")
                .setBic("bic-1").setTransactionId("t-100").setTimestamp(startDate).save(repo);
        PendingTransaction t101 = new PendingTransaction().setAmount("100")
                .setBic("bic-1").setTransactionId("t-101").setTimestamp(Dates.addHours(startDate, 5)).save(repo);
        PendingTransaction t102 = new PendingTransaction().setAmount("100")
                .setBic("bic-1").setTransactionId("t-102").setTimestamp(endDate).save(repo);
        PendingTransaction t103 = new PendingTransaction().setAmount("100")
                .setBic("bic-1").setTransactionId("t-103").setTimestamp(Dates.addHours(endDate, 1)).save(repo);
        PendingTransaction t104 = new PendingTransaction().setAmount("100")
                .setBic("bic-1").setTransactionId("t-104").setTimestamp(Dates.addHours(endDate, 23)).save(repo);
        new PendingTransaction().setAmount("100")
                .setBic("bic-1").setTransactionId("t-201").setTimestamp(Dates.addDays(endDate, 1)).save(repo);

        List<PendingTransaction> pendingTransactions = subject.getTransactionsFetchedFromBank("bic-1", startDate, endDate);
        assertEquals(5, pendingTransactions.size());
        assertTrue(pendingTransactions.contains(t100));
        assertTrue(pendingTransactions.contains(t101));
        assertTrue(pendingTransactions.contains(t102));
        assertTrue(pendingTransactions.contains(t103));
        assertTrue(pendingTransactions.contains(t104));
    }

    private void insertSeedTransactions() throws SQLException {
        new PendingTransaction().setAmount("100")
                .setBic("seed-bic-1").setTransactionId("seed-t1").setTimestamp(TestHelper.randomDate()).save(repo);
        new PendingTransaction().setAmount("100")
                .setBic("seed-bic-2").setTransactionId("seed-t2").setTimestamp(TestHelper.randomDate()).save(repo);
        new PendingTransaction().setAmount("100")
                .setBic("seed-bic-3").setTransactionId("seed-t3").setTimestamp(TestHelper.randomDate()).save(repo);
        new PendingTransaction().setAmount("100")
                .setTransactionId("seed-t4").setTimestamp(TestHelper.randomDate()).save(repo);
    }
}