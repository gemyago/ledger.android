package com.infora.ledger.data;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;

import com.infora.ledger.support.LogUtil;

/**
 * Created by jenya on 01.06.15.
 */
public class LedgerAccountsLoader extends AsyncTaskLoader<Cursor> {
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ACCOUNT_ID = "account_id";
    public static final String COLUMN_NAME = "name";

    public LedgerAccountsLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        LogUtil.d(this, "Forcing loading ledger accounts");
        forceLoad();
    }

    @Override
    public Cursor loadInBackground() {
        LogUtil.d(this, "Loading ledger accounts...");
        final MatrixCursor cursor = new MatrixCursor(new String[]{COLUMN_ID, COLUMN_ACCOUNT_ID, COLUMN_NAME});
        cursor.addRow(new Object[]{0, null, "Please select account..."});
        cursor.addRow(new Object[]{1, "100", "Account 100"});
        cursor.addRow(new Object[]{2, "101", "Account 101"});
        cursor.addRow(new Object[]{3, "102", "Account 102"});
        return cursor;

    }
}
