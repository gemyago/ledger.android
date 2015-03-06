package com.infora.ledger;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by jenya on 04.03.15.
 */
public class PendingTransactionsContentProvider extends ContentProvider {
    private static final String TAG = PendingTransactionsContentProvider.class.getName();

    public static final String AUTHORITY = PendingTransactionContract.AUTHORITY;
    public static final String PENDING_TRANSACTIONS_LIST_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + ".pending-transactions";
    public static final String PENDING_TRANSACTIONS_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + ".pending-transactions";

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
                String id = values.getAsString(PendingTransactionContract.COLUMN_ID);
                Log.d(TAG, "Inserting new transaction id='" + id + "'");
                dbHelper.getWritableDatabase().insert(PendingTransactionContract.TABLE_NAME, null, values);
                getContext().getContentResolver().notifyChange(uri, null);
                Uri.Builder builder = uri.buildUpon();
                builder.appendEncodedPath(id);
                return builder.build();
            default:
                throw newInvalidUrlException(uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
