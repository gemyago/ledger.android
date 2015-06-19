package com.infora.ledger.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.infora.ledger.DbUtils;
import com.infora.ledger.LedgerContentUris;
import com.infora.ledger.TransactionContract;

import java.util.Calendar;

public class PendingTransactionsContentProviderTest extends ProviderTestCase2<PendingTransactionsContentProvider> {

    private MockContentResolver resolver;
    private PendingTransactionsDbUtils repo;
    private LedgerDbHelper dbHelper;

    public PendingTransactionsContentProviderTest() {
        super(PendingTransactionsContentProvider.class, PendingTransactionsContentProvider.AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        resolver = getMockContentResolver();
        dbHelper = new LedgerDbHelper(getMockContext());
    }

    public void testGetType() throws Exception {
        Uri itemsUrl = Uri.parse("content://" + PendingTransactionsContentProvider.AUTHORITY + "/pending-transactions");
        assertEquals(PendingTransactionsContentProvider.PENDING_TRANSACTIONS_LIST_TYPE, resolver.getType(itemsUrl));

        Uri itemUrl = Uri.parse("content://" + PendingTransactionsContentProvider.AUTHORITY + "/pending-transactions/10");
        assertEquals(PendingTransactionsContentProvider.PENDING_TRANSACTIONS_ITEM_TYPE, resolver.getType(itemUrl));
    }

    public void testInsertNewTransaction() {
        ContentValues values = new ContentValues();
        values.put(TransactionContract.COLUMN_AMOUNT, "10.332");
        values.put(TransactionContract.COLUMN_COMMENT, "Comment 10.332");
        Uri newUri = resolver.insert(TransactionContract.CONTENT_URI, values);
        long id = ContentUris.parseId(newUri);
        assertFalse(id == 0);
        assertEquals(TransactionContract.CONTENT_URI + "/" + id, newUri.toString());

        PendingTransaction newTransaction = PendingTransactionsDbUtils.getById(dbHelper, id);
        assertNotNull(newTransaction.transactionId);
        assertNotNull(newTransaction.timestamp);
        assertEquals("10.332", newTransaction.amount);
        assertEquals("Comment 10.332", newTransaction.comment);
        assertFalse(newTransaction.isPublished);
    }

    public void testInsertNewTransactionWithExplicitTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -3);

        ContentValues values = new ContentValues();
        values.put(TransactionContract.COLUMN_AMOUNT, "10.332");
        values.put(TransactionContract.COLUMN_COMMENT, "Comment 10.332");
        values.put(TransactionContract.COLUMN_TIMESTAMP, LedgerDbHelper.toISO8601(calendar.getTime()));
        Uri newUri = resolver.insert(TransactionContract.CONTENT_URI, values);
        long id = ContentUris.parseId(newUri);

        PendingTransaction newTransaction = PendingTransactionsDbUtils.getById(dbHelper, id);
        assertEquals(newTransaction.timestamp, calendar.getTime());
    }

    public void testQuery() {
        DbUtils.insertPendingTransaction(dbHelper, "100", "100.00", "Transaction 100", false, false);
        DbUtils.insertPendingTransaction(dbHelper, "101", "101.00", "Transaction 101", true, false);
        DbUtils.insertPendingTransaction(dbHelper, "102", "102.00", "Transaction 102", true, false);

        Cursor results = resolver.query(TransactionContract.CONTENT_URI,
                TransactionContract.ALL_COLUMNS, null, null, TransactionContract.COLUMN_AMOUNT);

        assertEquals(3, results.getCount());
        results.moveToFirst();
        PendingTransaction t100 = new PendingTransaction(results);
        assertEquals("100", t100.transactionId);
        assertEquals("100.00", t100.amount);
        assertEquals("Transaction 100", t100.comment);
        assertEquals(false, t100.isPublished);

        results.moveToNext();
        PendingTransaction t101 = new PendingTransaction(results);
        assertEquals("101", t101.transactionId);
        assertEquals("101.00", t101.amount);
        assertEquals("Transaction 101", t101.comment);
        assertEquals(true, t101.isPublished);

        results.moveToNext();
        PendingTransaction t102 = new PendingTransaction(results);
        assertEquals("102", t102.transactionId);
        assertEquals("102.00", t102.amount);
        assertEquals("Transaction 102", t102.comment);
        assertEquals(true, t101.isPublished);
    }

    public void testQuerySkipDeleted() {
        DbUtils.insertPendingTransaction(dbHelper, "100", "100.00", "Transaction 100", false, false);
        DbUtils.insertPendingTransaction(dbHelper, "101", "101.00", "Transaction 101", false, true);
        DbUtils.insertPendingTransaction(dbHelper, "102", "102.00", "Transaction 102", false, true);

        Cursor results = resolver.query(TransactionContract.CONTENT_URI,
                TransactionContract.ASSIGNABLE_COLUMNS, TransactionContract.COLUMN_IS_DELETED + " = 0", null, TransactionContract.COLUMN_AMOUNT);

        assertEquals(1, results.getCount());
        results.moveToFirst();
        assertEquals("100", results.getString(results.getColumnIndexOrThrow(TransactionContract.COLUMN_TRANSACTION_ID)));
        assertEquals("100.00", results.getString(results.getColumnIndexOrThrow(TransactionContract.COLUMN_AMOUNT)));

    }

    public void testUpdate() {
        int id = DbUtils.insertPendingTransaction(dbHelper, "100", "100.00", "Transaction 100");

        ContentValues values = new ContentValues();
        values.put(TransactionContract.COLUMN_AMOUNT, "110.01");
        values.put(TransactionContract.COLUMN_COMMENT, "Transaction 110.01");
        values.put(TransactionContract.COLUMN_IS_PUBLISHED, true);
        values.put(TransactionContract.COLUMN_IS_DELETED, true);
        resolver.update(
                ContentUris.withAppendedId(TransactionContract.CONTENT_URI, id),
                values, null, null);

        PendingTransaction transaction = PendingTransactionsDbUtils.getById(dbHelper, id);
        assertEquals("110.01", transaction.amount);
        assertEquals("Transaction 110.01", transaction.comment);
        assertTrue("published flag was not updated", transaction.isPublished);
        assertTrue("deleted flag was not updated", transaction.isDeleted);
    }

    public void testDelete() {
        int id1 = DbUtils.insertPendingTransaction(dbHelper, "100", "100.00", "Transaction 100");
        int id2 = DbUtils.insertPendingTransaction(dbHelper, "101", "101.00", "Transaction 101");
        int id3 = DbUtils.insertPendingTransaction(dbHelper, "102", "102.00", "Transaction 102");

        assertEquals(1, resolver.delete(ContentUris.withAppendedId(TransactionContract.CONTENT_URI, id1), null, null));
        assertEquals(1, resolver.delete(ContentUris.withAppendedId(TransactionContract.CONTENT_URI, id3), null, null));

        Cursor results = resolver.query(TransactionContract.CONTENT_URI, null, null, null, null);
        assertEquals(results.getCount(), 1);
        results.moveToFirst();
        assertEquals(id2, new PendingTransaction(results).id);
    }
}