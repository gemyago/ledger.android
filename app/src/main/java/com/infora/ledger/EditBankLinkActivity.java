package com.infora.ledger;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

/**
 * Created by jenya on 01.06.15.
 */
public class EditBankLinkActivity extends AppCompatActivity {
    private static final String TAG = EditBankLinkActivity.class.getName();
    private static final int LEDGER_ACCOUNTS_LOADER = 1;
    private static final int LEDGER_BANK_LINK_LOADER = 2;

    private SimpleCursorAdapter spinnerAdapter;
    private BankLinksRepository bankLinksRepo;

    public BankLinksRepository getBankLinksRepo() {
        return bankLinksRepo == null ? (bankLinksRepo = new BankLinksRepository(this)) : bankLinksRepo;
    }

    public void setBankLinksRepo(BankLinksRepository bankLinksRepo) {
        this.bankLinksRepo = bankLinksRepo;
    }

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
        LogUtil.d(this, "initializing loaders");
//        getLoaderManager().initLoader(LEDGER_ACCOUNTS_LOADER, null, createAccountsLoader());
        long bankLinkId = getIntent().getLongExtra(BankLinksActivity.BANK_LINK_ID_EXTRA, 0);
        getLoaderManager().initLoader(LEDGER_BANK_LINK_LOADER, null, createBankLinkLoader(bankLinkId));
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

    private LoaderManager.LoaderCallbacks<Cursor> createAccountsLoader() {
        return new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new LedgerAccountsLoader(EditBankLinkActivity.this);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                spinnerAdapter.swapCursor(data);
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
                BusUtils.post(EditBankLinkActivity.this, new BankLinkLoaded());
            }

            @Override
            public void onLoaderReset(Loader<BankLink> loader) {

            }
        };
    }

    public static class BankLinkLoaded {
    }
}
