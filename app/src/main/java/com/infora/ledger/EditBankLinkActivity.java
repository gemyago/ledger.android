package com.infora.ledger;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.infora.ledger.data.LedgerAccountsLoader;
import com.infora.ledger.support.LogUtil;

/**
 * Created by jenya on 01.06.15.
 */
public class EditBankLinkActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = EditBankLinkActivity.class.getName();
    private static final int LEDGER_ACCOUNTS_LOADER = 1;

    private SimpleCursorAdapter spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bank_link);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Spinner ledgerAccountId = (Spinner) findViewById(R.id.ledger_account_id);
        spinnerAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item,
                null,
                new String[]{LedgerAccountsLoader.COLUMN_NAME},
                new int[]{android.R.id.text1},
                0);
        LogUtil.d(this, "assigning adapter");
        ledgerAccountId.setAdapter(spinnerAdapter);
        LogUtil.d(this, "initializing loader");
        getLoaderManager().initLoader(LEDGER_ACCOUNTS_LOADER, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        BusUtils.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        BusUtils.unregister(this);
    }

    public void updateBankLink(View view) {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "Creating loader");
        return new LedgerAccountsLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "Loading finished");
        spinnerAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        spinnerAdapter.swapCursor(null);
    }
}
