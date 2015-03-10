package com.infora.ledger.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.infora.ledger.PendingTransactionContract;

import java.util.Date;
import java.util.UUID;

/**
 * Created by jenya on 04.03.15.
 */
public class PendingTransactionsContentProvider extends ContentProvider {
    public static final String AUTHORITY = PendingTransactionContract.AUTHORITY;
    public static final String PENDING_TRANSACTIONS_LIST_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + ".pending-transactions";
    public static final String PENDING_TRANSACTIONS_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + ".pending-transactions";
    private static final String TAG = PendingTransactionsContentProvider.class.getName();
    private static final int TRANSACTIONS = 1000;
    private static final int TRANSACTION_ID = 1001;
    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "pending-transactions", TRANSACTIONS);
        sUriMatcher.addURI(AUTHORITY, "pending-transactions/#", TRANSACTION_ID);
    }

    private LedgerDbHelper dbHelper;

    private static IllegalArgumentException newInvalidUrlException(Uri uri) {
        return new IllegalArgumentException("The uri " + uri + " can not be matched.");
    }

    @Override
    public boolean onCreate() {
        dbHelper = new LedgerDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TRANSACTIONS:
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor query = db.query(PendingTransactionContract.TABLE_NAME, projection, null, null, null, null, sortOrder);
                query.setNotificationUri(getContext().getContentResolver(), uri);
                return query;
            default:
                throw newInvalidUrlException(uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TRANSACTIONS:
                return PENDING_TRANSACTIONS_LIST_TYPE;
            case TRANSACTION_ID:
                return PENDING_TRANSACTIONS_ITEM_TYPE;
            default:
                throw newInvalidUrlException(uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TRANSACTIONS:
                String transactionId = UUID.randomUUID().toString();
                values.put(PendingTransactionContract.COLUMN_TRANSACTION_ID, transactionId);
                values.put(PendingTransactionContract.COLUMN_TIMESTAMP, LedgerDbHelper.toISO8601(new Date()));
                Log.d(TAG, "Inserting new transaction transaction_id='" + transactionId + "'");
                long id = dbHelper.getWritableDatabase().insert(PendingTransactionContract.TABLE_NAME, null, values);
                notifyListChanged();
                return ContentUris.withAppendedId(uri, id);
            default:
                throw newInvalidUrlException(uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TRANSACTION_ID:
                long id = ContentUris.parseId(uri);
                Log.d(TAG, "Removing transaction id=" + id);
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                int deleted = db.delete(PendingTransactionContract.TABLE_NAME,
                        PendingTransactionContract.COLUMN_ID + " = ?",
                        new String[]{String.valueOf(id)});
                notifyListChanged();
                return deleted;
            default:
                throw newInvalidUrlException(uri);
        }

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private void notifyListChanged() {
        getContext().getContentResolver().notifyChange(PendingTransactionContract.CONTENT_URI, null);
    }
}
