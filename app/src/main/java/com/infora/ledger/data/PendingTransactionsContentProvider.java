package com.infora.ledger.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.infora.ledger.TransactionContract;

import java.util.Date;
import java.util.UUID;

/**
 * Created by jenya on 04.03.15.
 */
public class PendingTransactionsContentProvider extends ContentProvider {
    public static final String AUTHORITY = TransactionContract.AUTHORITY;
    public static final String PENDING_TRANSACTIONS_LIST_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + ".pending-transactions";
    public static final String PENDING_TRANSACTIONS_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + ".pending-transactions";
    private static final String TAG = PendingTransactionsContentProvider.class.getName();
    private static final int TRANSACTIONS = 1000;
    private static final int TRANSACTIONS_REPORTED_BY_USER = 1001;
    private static final int RECENT_TRANSACTION_FETCHED_FROM_BANK = 1002;
    private static final int TRANSACTION_ID = 1003;
    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "pending-transactions", TRANSACTIONS);
        sUriMatcher.addURI(AUTHORITY, "pending-transactions/reported-by-user", TRANSACTIONS_REPORTED_BY_USER);
        sUriMatcher.addURI(AUTHORITY, "pending-transactions/recent-fetched-from-bank/*", RECENT_TRANSACTION_FETCHED_FROM_BANK);
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
        Log.d(TAG, "Executing query: " + uri);
        final int match = sUriMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor query;
        switch (match) {
            case TRANSACTIONS:
                query = db.query(TransactionContract.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                query.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case TRANSACTIONS_REPORTED_BY_USER:
                if (selection != null)
                    throw new IllegalArgumentException("selection can not be provided for this query type");
                if (selectionArgs != null)
                    throw new IllegalArgumentException("selectionArgs can not be provided for this query type");
                selection = TransactionContract.COLUMN_BIC + " IS NULL";
                query = db.query(TransactionContract.TABLE_NAME, projection, selection, null, null, null, sortOrder);
                query.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case RECENT_TRANSACTION_FETCHED_FROM_BANK:
                if (selection != null)
                    throw new IllegalArgumentException("selection can not be provided for this query type");
                if (selectionArgs != null)
                    throw new IllegalArgumentException("selectionArgs can not be provided for this query type");
                if (sortOrder != null)
                    throw new IllegalArgumentException("sortOrder can not be provided for this query type");
                selection = TransactionContract.COLUMN_BIC + " = ?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                sortOrder = TransactionContract.COLUMN_TIMESTAMP + " DESC";
                query = db.query(TransactionContract.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder, "1");
                query.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            default:
                throw newInvalidUrlException(uri);
        }
        return query;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TRANSACTIONS:
                return PENDING_TRANSACTIONS_LIST_TYPE;
            case TRANSACTIONS_REPORTED_BY_USER:
                return PENDING_TRANSACTIONS_LIST_TYPE;
            case RECENT_TRANSACTION_FETCHED_FROM_BANK:
                return PENDING_TRANSACTIONS_ITEM_TYPE;
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
                values.put(TransactionContract.COLUMN_TRANSACTION_ID, transactionId);
                Date date = values.containsKey(TransactionContract.COLUMN_TIMESTAMP) ?
                        LedgerDbHelper.parseISO8601((String) values.get(TransactionContract.COLUMN_TIMESTAMP)) :
                        new Date();
                values.put(TransactionContract.COLUMN_TIMESTAMP, LedgerDbHelper.toISO8601(date));
                Log.d(TAG, "Inserting new transaction transaction_id='" + transactionId + "'");
                long id = dbHelper.getWritableDatabase().insert(TransactionContract.TABLE_NAME, null, values);
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
                int deleted = db.delete(TransactionContract.TABLE_NAME,
                        TransactionContract.COLUMN_ID + " = ?",
                        new String[]{String.valueOf(id)});
                notifyListChanged();
                return deleted;
            default:
                throw newInvalidUrlException(uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TRANSACTION_ID:
                long id = ContentUris.parseId(uri);
                Log.d(TAG, "Updating transaction id=" + id);
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                db.update(TransactionContract.TABLE_NAME,
                        values,
                        TransactionContract.COLUMN_ID + " = ?",
                        new String[]{String.valueOf(id)});
                notifyListChanged();
                return 1;
            default:
                throw newInvalidUrlException(uri);
        }
    }

    private void notifyListChanged() {
        getContext().getContentResolver().notifyChange(TransactionContract.CONTENT_URI, null);
    }
}
