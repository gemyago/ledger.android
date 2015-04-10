package com.infora.ledger;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.infora.ledger.data.LedgerDbHelper;
import com.infora.ledger.data.PendingTransactionsContentProvider;
import com.infora.ledger.data.PendingTransactionsDbUtils;

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
        assertNotNull(newTransaction.getTransactionId());
        assertNotNull(newTransaction.getTimestamp());
        assertEquals("10.332", newTransaction.getAmount());
        assertEquals("Comment 10.332", newTransaction.getComment());
        assertFalse(newTransaction.isPublished());
    }

    public void testQuery() {
        DbUtils.insertPendingTransaction(dbHelper, "100", "100.00", "Transaction 100", false);
        DbUtils.insertPendingTransaction(dbHelper, "101", "101.00", "Transaction 101", true);
        DbUtils.insertPendingTransaction(dbHelper, "102", "102.00", "Transaction 102", true);

        Cursor results = resolver.query(TransactionContract.CONTENT_URI,
                TransactionContract.ASSIGNABLE_COLUMNS, null, null, TransactionContract.COLUMN_AMOUNT);

        assertEquals(3, results.getCount());
        results.moveToFirst();
        assertEquals("100", results.getString(0));
        assertEquals("100.00", results.getString(1));
        assertEquals("Transaction 100", results.getString(2));
        assertEquals(0, results.getInt(3));

        results.moveToNext();
        assertEquals("101", results.getString(0));
        assertEquals("101.00", results.getString(1));
        assertEquals("Transaction 101", results.getString(2));
        assertEquals(1, results.getInt(3));

        results.moveToNext();
        assertEquals("102", results.getString(0));
        assertEquals("102.00", results.getString(1));
        assertEquals("Transaction 102", results.getString(2));
        assertEquals(1, results.getInt(3));
    }

    public void testUpdate() {
        int id = DbUtils.insertPendingTransaction(dbHelper, "100", "100.00", "Transaction 100");

        ContentValues values = new ContentValues();
        values.put(TransactionContract.COLUMN_AMOUNT, "110.01");
        values.put(TransactionContract.COLUMN_COMMENT, "Transaction 110.01");
        values.put(TransactionContract.COLUMN_IS_PUBLISHED, true);
        resolver.update(
                ContentUris.withAppendedId(TransactionContract.CONTENT_URI, id),
                values, null, null);

        PendingTransaction transaction = PendingTransactionsDbUtils.getById(dbHelper, id);
        assertEquals("110.01", transaction.getAmount());
        assertEquals("Transaction 110.01", transaction.getComment());
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
        assertEquals(id2, PendingTransaction.getId(results));
    }
}