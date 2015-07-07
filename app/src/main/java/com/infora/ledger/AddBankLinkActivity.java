package com.infora.ledger;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.infora.ledger.application.commands.AddBankLinkCommand;
import com.infora.ledger.application.events.AddBankLinkFailed;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.banks.ua.privatbank.PrivatBankTransaction;
import com.infora.ledger.banks.ua.urksibbank.UrksibBankTransaction;
import com.infora.ledger.data.LedgerAccountsLoader;
import com.infora.ledger.support.BusUtils;
import com.infora.ledger.support.LogUtil;
import com.infora.ledger.ui.BankLinkFragment;
import com.infora.ledger.ui.DatePickerFragment;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by jenya on 31.05.15.
 */
public class AddBankLinkActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = AddBankLinkActivity.class.getName();
    private static final int LEDGER_ACCOUNTS_LOADER = 1;

    private SimpleCursorAdapter accountsAdapter;

    private LedgerAccountsLoader.Factory accountsLoaderFactory;
    private Date initialFetchDate;
    private Spinner bic;
    private Spinner ledgerAccountId;
    private BankLinkFragment bankLinkFragment;
    private LinearLayout bankLinkFragmentContainer;
    private Button addButton;
    private TextView initialFetchDateText;

    private LedgerAccountsLoader.Factory getAccountsLoaderFactory() {
        return accountsLoaderFactory == null ?
                (accountsLoaderFactory = new LedgerAccountsLoader.Factory()) : accountsLoaderFactory;
    }

    public void setAccountsLoaderFactory(LedgerAccountsLoader.Factory accountsLoaderFactory) {
        this.accountsLoaderFactory = accountsLoaderFactory;
    }


    public Date getInitialFetchDate() {
        return initialFetchDate;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bank_link);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bic = (Spinner) findViewById(R.id.bic);
        ledgerAccountId = (Spinner) findViewById(R.id.ledger_account_id);
        bankLinkFragmentContainer = (LinearLayout) findViewById(R.id.bank_link_fragment_container);
        addButton = (Button) findViewById(R.id.action_add_bank_link);
        initialFetchDateText = (TextView) findViewById(R.id.initial_fetch_date);

        final ArrayAdapter<CharSequence> bicAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, new String[]{
                "", PrivatBankTransaction.PRIVATBANK_BIC, UrksibBankTransaction.BIC
        });
        bic.setAdapter(bicAdapter);
        bic.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                CharSequence bic = bicAdapter.getItem(i);
                if (bic == PrivatBankTransaction.PRIVATBANK_BIC) {
                    setBankLinkFragment(new PrivatBankLinkFragment());
                } else if (bic == UrksibBankTransaction.BIC) {
                    setBankLinkFragment(new UkrsibBankLinkFragment());
                } else {
                    setBankLinkFragment(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                setBankLinkFragment(null);
            }
        });

        accountsAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_spinner_item,
                null,
                new String[]{LedgerAccountsLoader.COLUMN_NAME},
                new int[]{android.R.id.text1},
                0);
        LogUtil.d(this, "assigning adapter");
        ledgerAccountId.setAdapter(accountsAdapter);
        LogUtil.d(this, "initializing loader");
        getLoaderManager().initLoader(LEDGER_ACCOUNTS_LOADER, null, this);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        initialFetchDate = c.getTime();
        initialFetchDateText.setText(DateFormat.getDateInstance().format(initialFetchDate));
    }

    private <TFragment extends Fragment & BankLinkFragment> void setBankLinkFragment(TFragment fragment) {
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        if (fragment == null) {
            Fragment oldFragment = getSupportFragmentManager().findFragmentByTag("bank-link-fragment");
            if (oldFragment != null) t.remove(oldFragment);
        } else {
            t.replace(R.id.bank_link_fragment_container, fragment, "bank-link-fragment");
        }
        t.commit();
        bankLinkFragment = fragment;
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
        AddBankLinkCommand command = new AddBankLinkCommand<>();

        Cursor selectedAccount = (Cursor) ledgerAccountId.getSelectedItem();
        if (selectedAccount != null) {
            command.accountId = selectedAccount.getString(selectedAccount.getColumnIndexOrThrow(LedgerAccountsLoader.COLUMN_ACCOUNT_ID));
            command.accountName = selectedAccount.getString(selectedAccount.getColumnIndexOrThrow(LedgerAccountsLoader.COLUMN_NAME));
        }

        if (command.accountId == null) {
            Toast.makeText(this, "Please select account", Toast.LENGTH_LONG).show();
            return;
        }

        command.bic = (String) bic.getSelectedItem();
        if (command.bic.isEmpty()) {
            Toast.makeText(this, "Please select bank", Toast.LENGTH_LONG).show();
            return;
        }

        command.initialFetchDate = initialFetchDate;
        command.linkData = bankLinkFragment.getBankLinkData();
        addButton.setEnabled(false);
        Log.d(TAG, "Posting command to create bank link");
        BusUtils.post(this, command);
    }

    public void changeInitialFetchDate(View view) {
        Log.d(TAG, "Showing date picker to change initial fetch date");
        DatePickerFragment fragment = new DatePickerFragment().setArguments(initialFetchDate);
        fragment.show(getSupportFragmentManager(), "change-initial-fetch-date-fragment");
    }

    public void onEventMainThread(BankLinkAdded event) {
        Log.d(TAG, "Bank link created. Resetting UI.");

        PrivatBankLinkFragment bankLinkFragment = (PrivatBankLinkFragment) getSupportFragmentManager().findFragmentById(R.id.bank_link_fragment_container);
        Spinner ledgerAccountId = (Spinner) findViewById(R.id.ledger_account_id);
        Button addButton = (Button) findViewById(R.id.action_add_bank_link);

        ledgerAccountId.setSelection(0);
        bankLinkFragment.clearLinkData();
        addButton.setEnabled(true);

        Toast.makeText(this, "Bank link added", Toast.LENGTH_SHORT).show();
    }

    public void onEventMainThread(AddBankLinkFailed event) {
        Button addButton = (Button) findViewById(R.id.action_add_bank_link);
        addButton.setEnabled(true);
        Toast.makeText(this, "Failure adding bank link: " + event.exception.getMessage(), Toast.LENGTH_LONG).show();
    }

    public void onEventMainThread(DatePickerFragment.DateChanged event) {
        Calendar c = Calendar.getInstance();
        c.set(event.year, event.month, event.day, 0, 0, 0);
        initialFetchDate = c.getTime();
        Log.d(TAG, "Changing initial fetch date to: " + initialFetchDate);
        initialFetchDateText.setText(DateFormat.getDateInstance().format(initialFetchDate));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        LogUtil.d(this, "Creating loader");
        return getAccountsLoaderFactory().createLoader(this).withSelectionPrompt();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        LogUtil.d(AddBankLinkActivity.this, "Loading finished");
        accountsAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        accountsAdapter.swapCursor(null);
    }
}
