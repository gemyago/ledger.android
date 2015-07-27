package com.infora.ledger;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.infora.ledger.data.LedgerAccountsLoader;
import com.infora.ledger.support.SpinnerSelector;

/**
 * Created by mye on 7/27/2015.
 */
public class DefaultTransactionAccountPreference extends DialogPreference {
    private static final String TAG = DefaultTransactionAccountPreference.class.getName();

    private static final int LEDGER_ACCOUNTS_LOADER = 1;
    private SimpleCursorAdapter accountsAdapter;
    private Spinner ledgerAccountId;

    public DefaultTransactionAccountPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.default_ledger_account_preference);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
    }

    @Override
    protected View onCreateDialogView() {
        View view = super.onCreateDialogView();
        ledgerAccountId = (Spinner) view.findViewById(R.id.ledger_account_id);

        accountsAdapter = new SimpleCursorAdapter(getContext(),
                android.R.layout.simple_spinner_item,
                null,
                new String[]{LedgerAccountsLoader.COLUMN_NAME},
                new int[]{android.R.id.text1},
                0);
        ledgerAccountId.setAdapter(accountsAdapter);
        Activity activity = (Activity) getContext();

        LoaderManager loaderManager = activity.getLoaderManager();

        Log.d(TAG, "Initializing accounts loader");
        loaderManager.initLoader(LEDGER_ACCOUNTS_LOADER, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                Log.d(TAG, "Creating the loader");
                return new LedgerAccountsLoader(getContext()).withSelectionPrompt();
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                Log.d(TAG, "Loading finished.");
                accountsAdapter.swapCursor(data);
                String accountId = getPersistedString(null);
                if (accountId != null) {
                    SpinnerSelector.select(ledgerAccountId, LedgerAccountsLoader.COLUMN_ACCOUNT_ID, accountId);
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                accountsAdapter.swapCursor(null);
            }
        });

        return view;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if(positiveResult) {
            Cursor selectedAccount = (Cursor) ledgerAccountId.getSelectedItem();
            String accountId = selectedAccount.getString(selectedAccount.getColumnIndexOrThrow(LedgerAccountsLoader.COLUMN_ACCOUNT_ID));
            persistString(accountId);
        }
    }
}
