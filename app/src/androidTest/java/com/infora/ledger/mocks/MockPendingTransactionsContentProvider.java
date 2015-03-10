package com.infora.ledger.mocks;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.util.Date;

/**
 * Created by jenya on 10.03.15.
 */
public class MockPendingTransactionsContentProvider extends ContentProvider {


    private Uri insertedUri;
    private InsertArgs insertArgs;
    private DeleteArgs deleteArgs;

    public InsertArgs getInsertArgs() {
        return insertArgs;
    }

    public void setInsertedUri(Uri insertedUri) {
        this.insertedUri = insertedUri;
    }

    public DeleteArgs getDeleteArgs() {
        return deleteArgs;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
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
}
