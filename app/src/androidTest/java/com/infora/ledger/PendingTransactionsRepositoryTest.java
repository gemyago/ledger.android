package com.infora.ledger;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

/**
 * Created by jenya on 01.03.15.
 */
public class PendingTransactionsRepositoryTest extends AndroidTestCase {

    private LedgerDbHelper dbHelper;
    private PendingTransactionsRepository subject;

    @Override
    public void setUp() throws Exception {
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "-pending-transactions-repo-test");
        DbUtils.deleteAllDatabases(context);
        dbHelper = new LedgerDbHelper(context);
        subject = new PendingTransactionsRepository(dbHelper);
    }

    public void testGetById() {
        DbUtils.insertPendingTransaction(dbHelper, "t-1", "100.32", "Transaction t-1");

        PendingTransaction actual = subject.getById("t-1");
        assertEquals("t-1", actual.getId());
        assertEquals("100.32", actual.getAmount());
        assertEquals("Transaction t-1", actual.getComment());
    }

    public void testGetByIdIfNoTransaction() {
        boolean thrown = false;
        try {
            subject.getById("unknown-t1");
        } catch (ObjectNotFoundException ex) {
            thrown = true;
        }
        assertTrue(thrown);
    }
    
    public void testSaveCreateNew() {
        subject.save(new PendingTransaction("t-100", "22.443", "t-100 comment"));

        PendingTransaction actual = subject.getById("t-100");
        assertEquals("t-100", actual.getId());
        assertEquals("22.443", actual.getAmount());
        assertEquals("t-100 comment", actual.getComment());
    }

    public void testSaveUpdateExisting() {
        DbUtils.insertPendingTransaction(dbHelper, "t-100", "0.00", "no comment");

        subject.save(new PendingTransaction("t-100", "22.443", "t-100 comment"));

        PendingTransaction actual = subject.getById("t-100");
        assertEquals("t-100", actual.getId());
        assertEquals("22.443", actual.getAmount());
        assertEquals("t-100 comment", actual.getComment());
    }
}
