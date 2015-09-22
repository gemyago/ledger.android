package com.infora.ledger.data;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.Log;

import com.infora.ledger.api.LedgerAccountDto;
import com.infora.ledger.api.LedgerApiFactory;
import com.infora.ledger.application.di.DiUtils;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by jenya on 01.06.15.
 */
public class LedgerAccountsLoader extends AsyncTaskLoader<Cursor> {
    private static final String TAG = LedgerAccountsLoader.class.getName();

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ACCOUNT_ID = "account_id";
    public static final String COLUMN_NAME = "name";

    @Inject LedgerApiFactory ledgerApiFactory;

    private boolean addSelectionPrompt;

    public LedgerAccountsLoader(Context context) {
        super(context);
        DiUtils.injector(context).inject(this);
    }

    public LedgerAccountsLoader(Context context, LedgerApiFactory ledgerApiFactory) {
        super(context);
        this.ledgerApiFactory = ledgerApiFactory;
    }

    public LedgerAccountsLoader withSelectionPrompt() {
        addSelectionPrompt = true;
        return this;
    }

    @Override
    protected void onStartLoading() {
        Log.d(TAG, "Forcing loading ledger accounts");
        forceLoad();
    }

    @Override
    public Cursor loadInBackground() {
        Log.d(TAG, "Loading ledger accounts...");
        final MatrixCursor cursor = new MatrixCursor(new String[]{COLUMN_ID, COLUMN_ACCOUNT_ID, COLUMN_NAME});
        if (addSelectionPrompt) cursor.addRow(new Object[]{0, null, "Please select account..."});
        List<LedgerAccountDto> accounts = ledgerApiFactory.createApi().getAccounts();
        int serialId = 0;
        for (LedgerAccountDto account : accounts) {
            cursor.addRow(new Object[]{++serialId, account.id, account.name});
        }
        return cursor;
    }

    public static class Factory {
        @Inject public Factory() {
        }

        public LedgerAccountsLoader createLoader(Context context) {
            return new LedgerAccountsLoader(context);
        }
    }
}
