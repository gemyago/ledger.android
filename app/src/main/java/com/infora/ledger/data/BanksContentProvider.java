package com.infora.ledger.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.infora.ledger.BanksContract;
import com.infora.ledger.TransactionContract;
import com.infora.ledger.support.LogUtil;

/**
 * Created by jenya on 30.05.15.
 */
public class BanksContentProvider extends ContentProvider {
    public static final String AUTHORITY = BanksContract.AUTHORITY;
    public static final String BANK_LINKS_LIST_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + ".banks.bank-links";
    public static final String BANK_LINKS_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + ".banks.bank-links";
    private static final UriMatcher sUriMatcher;

    private static final int BANKS_BANK_LINKS = 1000;
    private static final int BANKS_BANK_LINK = 1001;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "banks/bank-links", BANKS_BANK_LINKS);
        sUriMatcher.addURI(AUTHORITY, "banks/bank-links/#", BANKS_BANK_LINK);
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
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BANKS_BANK_LINKS:
                return BANK_LINKS_LIST_TYPE;
            case BANKS_BANK_LINK:
                return BANK_LINKS_ITEM_TYPE;
            default:
                throw newInvalidUrlException(uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        LedgerDbHelper dbHelper = new LedgerDbHelper(getContext());
        switch (match) {
            case BANKS_BANK_LINKS:
                String bic = values.getAsString(BanksContract.BankLinks.COLUMN_BIC);
                LogUtil.d(this, "Inserting new bank_link for bank='" + bic + "'");
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                try {
                    long id = db.insert(BanksContract.BankLinks.TABLE_NAME, null, values);
                    notifyListChanged();
                    return ContentUris.withAppendedId(uri, id);
                } finally {
                    db.close();
                }
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

    private static IllegalArgumentException newInvalidUrlException(Uri uri) {
        return new IllegalArgumentException("The uri " + uri + " can not be matched.");
    }

    private void notifyListChanged() {
        getContext().getContentResolver().notifyChange(TransactionContract.CONTENT_URI, null);
    }
}
