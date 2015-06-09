package com.infora.ledger;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.infora.ledger.application.commands.AddBankLinkCommand;
import com.infora.ledger.application.events.AddBankLinkFailed;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.banks.PrivatBankLinkData;
import com.infora.ledger.banks.PrivatBankTransaction;
import com.infora.ledger.data.LedgerAccountsLoader;
import com.infora.ledger.support.BusUtils;
import com.infora.ledger.support.LogUtil;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by jenya on 31.05.15.
 */
public class AddBankLinkActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = AddBankLinkActivity.class.getName();
    private static final int LEDGER_ACCOUNTS_LOADER = 1;

    private SimpleCursorAdapter spinnerAdapter;

    private Date initialFetchDate;
    private Spinner ledgerAccountId;
    private PrivatBankLinkFragment bankLinkFragment;
    private Button addButton;
    private TextView initialFetchDateText;

    public Date getInitialFetchDate() {
        return initialFetchDate;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bank_link);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ledgerAccountId = (Spinner) findViewById(R.id.ledger_account_id);
        bankLinkFragment = (PrivatBankLinkFragment) getSupportFragmentManager().findFragmentById(R.id.bank_link_fragment);
        addButton = (Button) findViewById(R.id.action_add_bank_link);
        initialFetchDateText = (TextView) findViewById(R.id.initial_fetch_date);

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
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        initialFetchDate = c.getTime();
        initialFetchDateText.setText(DateFormat.getDateInstance().format(initialFetchDate));
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
        AddBankLinkCommand<PrivatBankLinkData> command = new AddBankLinkCommand<>();
        Cursor selectedAccount = (Cursor) ledgerAccountId.getSelectedItem();
        command.accountId = selectedAccount.getString(selectedAccount.getColumnIndexOrThrow(LedgerAccountsLoader.COLUMN_ACCOUNT_ID));
        command.accountName = selectedAccount.getString(selectedAccount.getColumnIndexOrThrow(LedgerAccountsLoader.COLUMN_NAME));
        command.bic = PrivatBankTransaction.PRIVATBANK_BIC;
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

        PrivatBankLinkFragment bankLinkFragment = (PrivatBankLinkFragment) getSupportFragmentManager().findFragmentById(R.id.bank_link_fragment);
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

    public void onEventMainThread(ChangeInitialFetchDate event) {
        Calendar c = Calendar.getInstance();
        c.set(event.year, event.month, event.day, 0, 0, 0);
        initialFetchDate = c.getTime();
        Log.d(TAG, "Changing initial fetch date to: " + initialFetchDate);
        initialFetchDateText.setText(DateFormat.getDateInstance().format(initialFetchDate));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        LogUtil.d(this, "Creating loader");
        return new LedgerAccountsLoader(this).withSelectionPrompt();
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

    public static class DatePickerFragment extends DialogFragment {
        private Date initialDate;

        public DatePickerFragment setArguments(Date initialDate) {
            this.initialDate = initialDate;
            return this;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            c.setTime(initialDate);
            int initialYear = c.get(Calendar.YEAR);
            int initialMonth = c.get(Calendar.MONTH);
            int initialDay = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), null, initialYear, initialMonth, initialDay) {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == BUTTON_POSITIVE) {
                        int year = getDatePicker().getYear();
                        int month = getDatePicker().getMonth();
                        int day = getDatePicker().getDayOfMonth();

                        BusUtils.post(getActivity(), new ChangeInitialFetchDate(year, month, day));
                    }
                    super.onClick(dialog, which);
                }
            };
        }
    }

    public static class ChangeInitialFetchDate {
        int year;
        int month;
        int day;

        public ChangeInitialFetchDate(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }
    }

}
