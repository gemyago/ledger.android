package com.infora.ledger.mocks;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import com.infora.ledger.PendingTransaction;
import com.infora.ledger.TransactionContract;
import com.infora.ledger.data.LedgerDbHelper;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by jenya on 10.03.15.
 */
public class MockPendingTransactionsContentProvider extends ContentProvider {
    private static final String TAG = MockPendingTransactionsContentProvider.class.getName();

    public MockPendingTransactionsContentProvider(Context context) {
        attachInfo(context, null);
    }

    private Uri insertedUri;
    private InsertArgs insertArgs;
    private DeleteArgs deleteArgs;
    private QueryArgs queryArgs;
    private UpdateArgs updateArgs;
    private ArrayList<UpdateArgs> allUpdateArgs = new ArrayList<>();
    private Cursor queryResult;

    public QueryArgs getQueryArgs() {
        return queryArgs;
    }

    public InsertArgs getInsertArgs() {
        return insertArgs;
    }

    public UpdateArgs getUpdateArgs() {
        return updateArgs;
    }

    public ArrayList<UpdateArgs> getAllUpdateArgs() {
        return allUpdateArgs;
    }

    public void setInsertedUri(Uri insertedUri) {
        this.insertedUri = insertedUri;
    }

    public DeleteArgs getDeleteArgs() {
        return deleteArgs;
    }

    public void setQueryResult(Cursor queryResult) {
        this.queryResult = queryResult;
    }

    public void setQueryResult(PendingTransaction... transactions) {
        MatrixCursor matrixCursor = new MatrixCursor(TransactionContract.ALL_COLUMNS);
        for (PendingTransaction transaction : transactions) {
            matrixCursor.addRow(new Object[]{
                    transaction.getId(),
                    transaction.getTransactionId(),
                    transaction.getAmount(),
                    transaction.getComment(),
                    transaction.isPublished() ? 1 : 0,
                    transaction.isDeleted() ? 1 : 0,
                    LedgerDbHelper.toISO8601(transaction.getTimestamp() == null ? new Date() : transaction.getTimestamp())});
        }
        setQueryResult(matrixCursor);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "Executing query: " + uri);
        queryArgs = new QueryArgs(uri, projection, selection, selectionArgs, sortOrder);
        return queryResult;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        insertArgs = new InsertArgs(uri, values);
        if (insertedUri == null) return ContentUris.withAppendedId(uri, new Date().getTime());
        return insertedUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        deleteArgs = new DeleteArgs(uri, selection, selectionArgs);
        return 1;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        updateArgs = new UpdateArgs(uri, values, selection, selectionArgs);
        allUpdateArgs.add(updateArgs);
        return 0;
    }

    public static class InsertArgs {

        private final Uri uri;
        private final ContentValues values;

        public InsertArgs(Uri uri, ContentValues values) {
            this.uri = uri;
            this.values = values;
        }

        public Uri getUri() {
            return uri;
        }

        public ContentValues getValues() {
            return values;
        }
    }

    public class UpdateArgs {
        public final Uri uri;
        public final ContentValues values;
        public final String selection;
        public final String[] selectionArgs;

        public UpdateArgs(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            this.uri = uri;
            this.values = values;
            this.selection = selection;
            this.selectionArgs = selectionArgs;
        }
    }

    public static class DeleteArgs {
        private final Uri uri;
        private final String selection;

        private final String[] selectionArgs;

        private DeleteArgs(Uri uri, String selection, String[] selectionArgs) {
            this.uri = uri;
            this.selection = selection;
            this.selectionArgs = selectionArgs;
        }

        public Uri getUri() {
            return uri;
        }

        public String getSelection() {
            return selection;
        }

        public String[] getSelectionArgs() {
            return selectionArgs;
        }

    }

    public class QueryArgs {
        public final Uri uri;
        public final String[] projection;
        public final String selection;
        public final String[] selectionArgs;
        public final String sortOrder;

        public QueryArgs(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
            this.uri = uri;
            this.projection = projection;
            this.selection = selection;
            this.selectionArgs = selectionArgs;
            this.sortOrder = sortOrder;
        }

    }
}
