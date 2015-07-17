package com.infora.ledger;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.infora.ledger.application.DeviceSecretProvider;
import com.infora.ledger.application.commands.UpdateBankLinkCommand;
import com.infora.ledger.application.events.BankLinkUpdated;
import com.infora.ledger.application.events.UpdateBankLinkFailed;
import com.infora.ledger.banks.ua.privatbank.PrivatBankLinkData;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseRepository;
import com.infora.ledger.data.LedgerAccountsLoader;
import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.support.BusUtils;
import com.infora.ledger.support.LogUtil;
import com.infora.ledger.ui.BankLinkFragment;
import com.infora.ledger.ui.BankLinkFragmentsFactory;
import com.infora.ledger.ui.DatePickerFragment;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * Created by jenya on 01.06.15.
 */
public class EditBankLinkActivity extends AppCompatActivity {
    public static final String BANK_LINK_FRAGMENT = "bank-link-fragment";
    private static final String TAG = EditBankLinkActivity.class.getName();
    private static final int LEDGER_ACCOUNTS_LOADER = 1;
    private static final int LEDGER_BANK_LINK_LOADER = 2;

    private SimpleCursorAdapter spinnerAdapter;
    private DatabaseRepository<BankLink> bankLinksRepo;
    private LedgerAccountsLoader.Factory accountsLoaderFactory;
    private BankLinkFragment bankLinkFragment;
    private Button updateButton;
    private Spinner accountsSpinner;
    private long bankLinkId;
    private Date fetchStartingFrom;
    private BankLinkFragmentsFactory bankLinkFragmentsFactory;
    private DeviceSecretProvider deviceSecretProvider;
    private EditText bic;

    public BankLinkFragmentsFactory getBankLinkFragmentsFactory() {
        return bankLinkFragmentsFactory == null ?
                (bankLinkFragmentsFactory = BankLinkFragmentsFactory.createDefault()) : bankLinkFragmentsFactory;
    }

    public void setBankLinkFragmentsFactory(BankLinkFragmentsFactory bankLinkFragmentsFactory) {
        this.bankLinkFragmentsFactory = bankLinkFragmentsFactory;
    }

    public DatabaseRepository<BankLink> getBankLinksRepo() {
        return bankLinksRepo == null ? (bankLinksRepo = DatabaseContext.getInstance(this).createRepository(BankLink.class)) : bankLinksRepo;
    }

    public void setBankLinksRepo(DatabaseRepository bankLinksRepo) {
        this.bankLinksRepo = bankLinksRepo;
    }

    private LedgerAccountsLoader.Factory getAccountsLoaderFactory() {
        return accountsLoaderFactory == null ?
                (accountsLoaderFactory = new LedgerAccountsLoader.Factory()) : accountsLoaderFactory;
    }

    public void setAccountsLoaderFactory(LedgerAccountsLoader.Factory accountsLoaderFactory) {
        this.accountsLoaderFactory = accountsLoaderFactory;
    }

    public DeviceSecretProvider getDeviceSecretProvider() {
        return deviceSecretProvider == null ?
                (deviceSecretProvider = ((LedgerApplication)getApplication()).getDeviceSecretProvider()) : deviceSecretProvider;
    }

    public void setDeviceSecretProvider(DeviceSecretProvider deviceSecretProvider) {
        this.deviceSecretProvider = deviceSecretProvider;
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

        bankLinkId = getIntent().getLongExtra(BankLinksActivity.BANK_LINK_ID_EXTRA, 0);
        bic = (EditText) findViewById(R.id.bic);
        updateButton = (Button) findViewById(R.id.action_update_bank_link);
        accountsSpinner = (Spinner) findViewById(R.id.ledger_account_id);

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

    public void updateBankLink(View view) {
        Cursor selectedItem = (Cursor) accountsSpinner.getSelectedItem();
        UpdateBankLinkCommand command = new UpdateBankLinkCommand(
                (int) bankLinkId,
                selectedItem.getString(selectedItem.getColumnIndexOrThrow(LedgerAccountsLoader.COLUMN_ACCOUNT_ID)),
                selectedItem.getString(selectedItem.getColumnIndexOrThrow(LedgerAccountsLoader.COLUMN_NAME)),
                bankLinkFragment.getBankLinkData());
        if(fetchStartingFrom != null) command.setFetchFromDate(fetchStartingFrom);
        BusUtils.post(this, command);
        updateButton.setEnabled(false);
    }

    public void changeFetchStartingFromDate(View view) {
        Log.d(TAG, "Showing date picker to change fetch starting from date");
        DatePickerFragment fragment = new DatePickerFragment().setArguments(fetchStartingFrom == null ? new Date() : fetchStartingFrom);
        fragment.show(getSupportFragmentManager(), "change-fetch-starting-from");
    }


    public void onEventMainThread(BankLinkUpdated event) {
        updateButton.setEnabled(true);
        Toast.makeText(this, "Bank link updated", Toast.LENGTH_SHORT).show();
    }

    public void onEventMainThread(UpdateBankLinkFailed event) {
        updateButton.setEnabled(true);
        Toast.makeText(this, "Failure adding bank link: " + event.exception.getMessage(), Toast.LENGTH_LONG).show();
    }


    public void onEvent(DatePickerFragment.DateChanged event) {
        Calendar c = Calendar.getInstance();
        c.set(event.year, event.month, event.day, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        fetchStartingFrom = c.getTime();
        Log.d(TAG, "Date to fetch starting from assigned to: " + fetchStartingFrom);
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
                            getDeviceSecretProvider().ensureDeviceRegistered();
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
                Log.d(TAG, "Bank link data bic='" + data.bic + "' loaded.");
                bic.setText(data.bic);
                bankLinkFragment = getBankLinkFragmentsFactory().get(data.bic);
                bankLinkFragment.setBankLinkData(data, getDeviceSecretProvider().secret());
                FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                t.replace(R.id.bank_link_fragment_container, bankLinkFragment, BANK_LINK_FRAGMENT);
                t.commit();

                for (int i = 0; i < accountsSpinner.getCount(); i++) {
                    Cursor account = (Cursor) accountsSpinner.getItemAtPosition(i);
                    String accountId = account.getString(account.getColumnIndexOrThrow(LedgerAccountsLoader.COLUMN_ACCOUNT_ID));
                    if (Objects.equals(data.accountId, accountId)) {
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
