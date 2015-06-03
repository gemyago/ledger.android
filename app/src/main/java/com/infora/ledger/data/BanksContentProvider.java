package com.infora.ledger.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.infora.ledger.BanksContract;
import com.infora.ledger.LedgerApplication;
import com.infora.ledger.TransactionContract;
import com.infora.ledger.application.events.BankLinkUpdated;
import com.infora.ledger.application.events.BankLinksDeletedEvent;
import com.infora.ledger.support.BusUtils;
import com.infora.ledger.support.LogUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 30.05.15.
 */
public class BanksContentProvider extends ContentProvider {
    private static final String TAG = BanksContentProvider.class.getName();

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

    private EventBus bus;

    public EventBus getBus() {
        if (bus == null) {
            //In tests there is no easy way to provide custom application
            //So just creating the bus instance to make the provider work in tests.
            Context applicationContext = getContext().getApplicationContext();
            if(applicationContext instanceof LedgerApplication) {
                bus = ((LedgerApplication) applicationContext).getBus();
            } else {
                bus = new EventBus();
            }
        }
        return bus;
    }

    @Override
    public boolean onCreate() {
        Log.d(TAG, "Initializing provider. Subscribing to the bus...");
        getBus().register(this);
        return true;
    }

    @Override
    public void shutdown() {
        Log.d(TAG, "Provider shutdown.");
        super.shutdown();
        getBus().unregister(this);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final int match = sUriMatcher.match(uri);
        LedgerDbHelper dbHelper = new LedgerDbHelper(getContext());
        switch (match) {
            case BANKS_BANK_LINKS:
                LogUtil.d(this, "Querying bank links...");
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor query = db.query(BanksContract.BankLinks.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
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

    public void onEvent(BankLinksDeletedEvent event) {
        notifyListChanged();
    }
    public void onEvent(BankLinkUpdated event) {
        notifyListChanged();
    }

    private void notifyListChanged() {
        Log.d(TAG, "Notifying " + BanksContract.BankLinks.CONTENT_URI + " changed.");
        getContext().getContentResolver().notifyChange(BanksContract.BankLinks.CONTENT_URI, null);
    }
}
