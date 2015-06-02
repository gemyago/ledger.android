package com.infora.ledger;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.CursorWrapper;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.infora.ledger.banks.PrivatBankLinkData;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.BankLinksRepository;
import com.infora.ledger.data.LedgerAccountsLoader;
import com.infora.ledger.support.BusUtils;
import com.infora.ledger.support.LogUtil;

import java.sql.SQLException;
import java.util.Objects;

/**
 * Created by jenya on 01.06.15.
 */
public class EditBankLinkActivity extends AppCompatActivity {
    private static final String TAG = EditBankLinkActivity.class.getName();
    private static final int LEDGER_ACCOUNTS_LOADER = 1;
    private static final int LEDGER_BANK_LINK_LOADER = 2;

    private SimpleCursorAdapter spinnerAdapter;
    private BankLinksRepository bankLinksRepo;
    private LedgerAccountsLoader.Factory accountsLoaderFactory;

    public BankLinksRepository getBankLinksRepo() {
        return bankLinksRepo == null ? (bankLinksRepo = new BankLinksRepository(this)) : bankLinksRepo;
    }

    public void setBankLinksRepo(BankLinksRepository bankLinksRepo) {
        this.bankLinksRepo = bankLinksRepo;
    }

    private LedgerAccountsLoader.Factory getAccountsLoaderFactory() {
        return accountsLoaderFactory == null ?
                (accountsLoaderFactory = new LedgerAccountsLoader.Factory()) : accountsLoaderFactory;
    }

    public void setAccountsLoaderFactory(LedgerAccountsLoader.Factory accountsLoaderFactory) {
        this.accountsLoaderFactory = accountsLoaderFactory;
    }

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
        ledgerAccountId.setAdapter(spinnerAdapter);
        LogUtil.d(this, "initializing loaders");
        getLoaderManager().initLoader(LEDGER_ACCOUNTS_LOADER, null, createAccountsLoaderCallbacks());
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

    private LoaderManager.LoaderCallbacks<Cursor> createAccountsLoaderCallbacks() {
        return new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                Log.d(TAG, "Creating accounts loader.");
                return getAccountsLoaderFactory().createLoader(EditBankLinkActivity.this);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                spinnerAdapter.swapCursor(data);
                BusUtils.post(EditBankLinkActivity.this, new AccountsLoaded());

                long bankLinkId = getIntent().getLongExtra(BankLinksActivity.BANK_LINK_ID_EXTRA, 0);
                getLoaderManager().initLoader(LEDGER_BANK_LINK_LOADER, null, createBankLinkLoader(bankLinkId));
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                spinnerAdapter.swapCursor(null);
            }
        };
    }

    private LoaderManager.LoaderCallbacks<BankLink> createBankLinkLoader(final long bankLinkId) {
        return new LoaderManager.LoaderCallbacks<BankLink>() {
            @Override
            public Loader<BankLink> onCreateLoader(int id, Bundle args) {
                Log.d(TAG, "Creating bank link loader.");
                AsyncTaskLoader<BankLink> bankLinkLoader = new AsyncTaskLoader<BankLink>(EditBankLinkActivity.this) {
                    @Override
                    protected void onStartLoading() {
                        LogUtil.d(this, "Forcing loading bank link");
                        forceLoad();
                    }

                    @Override
                    public BankLink loadInBackground() {
                        Log.d(TAG, "Loading bank link data.");
                        try {
                            return getBankLinksRepo().getById(bankLinkId);
                        } catch (SQLException e) {
                            //TODO: Implement error handling.
                            throw new RuntimeException(e);
                        }
                    }
                };
                return bankLinkLoader;
            }

            @Override
            public void onLoadFinished(Loader<BankLink> loader, BankLink data) {
                Log.d(TAG, "Bank link data id='" + data.id + "' loaded.");
                PrivatBankLinkFragment bankLinkFragment = (PrivatBankLinkFragment) getSupportFragmentManager().findFragmentById(R.id.bank_link_fragment);
                bankLinkFragment.setBankLinkData(data.getLinkData(PrivatBankLinkData.class));

                Spinner accountsSpinner = (Spinner) findViewById(R.id.ledger_account_id);
                for (int i = 0; i < accountsSpinner.getCount(); i++) {
                    Cursor account = (Cursor) accountsSpinner.getItemAtPosition(i);
                    String accountId = account.getString(account.getColumnIndexOrThrow(LedgerAccountsLoader.COLUMN_ACCOUNT_ID));
                    if(Objects.equals(data.accountId, accountId)) {
                        accountsSpinner.setSelection(i);
                        break;
                    }
                }

                BusUtils.post(EditBankLinkActivity.this, new BankLinkLoaded());
            }

            @Override
            public void onLoaderReset(Loader<BankLink> loader) {

            }
        };
    }

    public static class BankLinkLoaded {
    }

    public static class AccountsLoaded {
    }
}
