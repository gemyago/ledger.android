package com.infora.ledger.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.infora.ledger.BanksContract;

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
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
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
