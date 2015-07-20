package com.infora.ledger.data;

import android.accounts.Account;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.Log;

import com.infora.ledger.SettingsFragment;
import com.infora.ledger.api.ApiAdapter;
import com.infora.ledger.api.LedgerAccountDto;
import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.support.AccountManagerWrapper;
import com.infora.ledger.support.LogUtil;
import com.infora.ledger.support.SharedPreferencesUtil;

import java.util.List;

/**
 * Created by jenya on 01.06.15.
 */
public class LedgerAccountsLoader extends AsyncTaskLoader<Cursor> {
    private static final String TAG = LedgerAccountsLoader.class.getName();

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ACCOUNT_ID = "account_id";
    public static final String COLUMN_NAME = "name";

    private LedgerApi ledgerApi;
    private boolean addSelectionPrompt;

    public LedgerAccountsLoader(Context context) {
        super(context);
    }

    public LedgerAccountsLoader(Context context, LedgerApi ledgerApi) {
        super(context);
        this.ledgerApi = ledgerApi;
    }

    public LedgerAccountsLoader withSelectionPrompt() {
        addSelectionPrompt = true;
        return this;
    }

    @Override
    protected void onStartLoading() {
        LogUtil.d(this, "Forcing loading ledger accounts");
        forceLoad();
    }

    @Override
    public Cursor loadInBackground() {
        if (ledgerApi == null) ledgerApi = createApi(getContext());
        LogUtil.d(this, "Loading ledger accounts...");
        final MatrixCursor cursor = new MatrixCursor(new String[]{COLUMN_ID, COLUMN_ACCOUNT_ID, COLUMN_NAME});
        if (addSelectionPrompt) cursor.addRow(new Object[]{0, null, "Please select account..."});
        List<LedgerAccountDto> accounts = ledgerApi.getAccounts();
        int serialId = 0;
        for (LedgerAccountDto account : accounts) {
            cursor.addRow(new Object[]{++serialId, account.id, account.name});
        }
        return cursor;

    }

    private LedgerApi createApi(Context context) {
        SharedPreferences prefs = SharedPreferencesUtil.getDefaultSharedPreferences(context);
        String ledgerHost = prefs.getString(SettingsFragment.KEY_LEDGER_HOST, null);
        Log.d(TAG, "Using ledger host: " + ledgerHost);
        AccountManagerWrapper accountManager = new AccountManagerWrapper(context);
        ApiAdapter apiAdapter = new ApiAdapter(context, accountManager, ledgerHost);
        LedgerApi api = apiAdapter.createApi();
        Account[] accounts = accountManager.getApplicationAccounts();
        if (accounts.length == 0)
            throw new IllegalStateException("The application has no accounts assigned");
        Log.d(TAG, "Authenticating api");
        apiAdapter.authenticateApi(api, accounts[0]);
        return api;
    }

    public static class Factory {
        public LedgerAccountsLoader createLoader(Context context) {
            return new LedgerAccountsLoader(context);
        }
    }
}
