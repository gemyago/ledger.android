package com.infora.ledger;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.infora.ledger.application.commands.AddBankLinkCommand;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.banks.PrivatBankLinkData;
import com.infora.ledger.banks.PrivatBankTransaction;
import com.infora.ledger.data.LedgerAccountsLoader;
import com.infora.ledger.support.BusUtils;
import com.infora.ledger.support.LogUtil;

/**
 * Created by jenya on 31.05.15.
 */
public class AddBankLinkActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = AddBankLinkActivity.class.getName();
    private static final int LEDGER_ACCOUNTS_LOADER = 1;

    private SimpleCursorAdapter spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bank_link);
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
        BusUtils.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        BusUtils.unregister(this);
    }

    public void addBankLink(View view) {
        Spinner ledgerAccountId = (Spinner) findViewById(R.id.ledger_account_id);
        PrivatBankLinkFragment bankLinkFragment = (PrivatBankLinkFragment) getSupportFragmentManager().findFragmentById(R.id.bank_link_fragment);
        Button addButton = (Button) findViewById(R.id.action_add_bank_link);

        AddBankLinkCommand<PrivatBankLinkData> command = new AddBankLinkCommand<>();
        Cursor selectedAccount = (Cursor) ledgerAccountId.getSelectedItem();
        command.accountId = selectedAccount.getString(selectedAccount.getColumnIndexOrThrow(LedgerAccountsLoader.COLUMN_ACCOUNT_ID));
        command.accountName = selectedAccount.getString(selectedAccount.getColumnIndexOrThrow(LedgerAccountsLoader.COLUMN_NAME));
        command.bic = PrivatBankTransaction.PRIVATBANK_BIC;
        command.linkData = bankLinkFragment.getBankLinkData();
        addButton.setEnabled(false);
        Log.d(TAG, "Posting command to create bank link");
        BusUtils.post(this, command);
    }

    public void onEventMainThread(BankLinkAdded event) {
        Log.d(TAG, "Bank link created. Resetting UI.");

        PrivatBankLinkFragment bankLinkFragment = (PrivatBankLinkFragment) getSupportFragmentManager().findFragmentById(R.id.bank_link_fragment);
        Spinner ledgerAccountId = (Spinner) findViewById(R.id.ledger_account_id);
        Button addButton = (Button) findViewById(R.id.action_add_bank_link);

        ledgerAccountId.setSelection(0);
        bankLinkFragment.clearLinkData();
        addButton.setEnabled(true);

        Toast.makeText(this, "Bank link added", Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        LogUtil.d(this, "Creating loader");
        return new LedgerAccountsLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        LogUtil.d(AddBankLinkActivity.this, "Loading finished");
        spinnerAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        spinnerAdapter.swapCursor(null);
    }
}
