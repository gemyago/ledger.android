package com.infora.ledger;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.infora.ledger.application.commands.DeleteTransactionsCommand;
import com.infora.ledger.application.commands.ReportTransactionCommand;
import com.infora.ledger.application.di.DiUtils;
import com.infora.ledger.application.events.TransactionReportedEvent;
import com.infora.ledger.application.events.TransactionsDeletedEvent;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.data.TransactionsReadModel;
import com.infora.ledger.support.BusUtils;
import com.infora.ledger.support.EventHandler;
import com.infora.ledger.support.SharedPreferencesUtil;

import java.sql.SQLException;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class ReportActivity extends AppCompatActivity {
    private static final String TAG = ReportActivity.class.getName();
    private static final int REPORTED_TRANSACTIONS_LOADER_ID = 1;
    private SimpleCursorAdapter reportedTransactionsAdapter;

    @Bind(R.id.reported_transactions_list) ListView lvReportedTransactions;
    @Bind(R.id.comment) EditText comment;
    @Bind(R.id.amount) EditText amount;
    @Bind(R.id.report) Button report;

    @Inject EventBus bus;
    @Inject TransactionsReadModel transactionsReadModel;

    public static final String EDIT_TRANSACTION_DIALOG_TAG = "EditTransactionDialog";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        ButterKnife.bind(this);
        DiUtils.injector(this).inject(this);

        reportedTransactionsAdapter = new SimpleCursorAdapter(this, R.layout.transactions_list,
                null,
                new String[]{TransactionContract.COLUMN_AMOUNT, TransactionContract.COLUMN_COMMENT},
                new int[]{R.id.amount, R.id.comment}, 0);

        lvReportedTransactions.setAdapter(reportedTransactionsAdapter);
        getLoaderManager().initLoader(REPORTED_TRANSACTIONS_LOADER_ID, null, new LoaderCallbacks());

        lvReportedTransactions.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lvReportedTransactions.setMultiChoiceModeListener(new ModeCallback());
        lvReportedTransactions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Editing transaction: " + id);
                EditTransactionDialog dialog = new EditTransactionDialog();
                CursorWrapper clickedItem = (CursorWrapper) lvReportedTransactions.getItemAtPosition(position);
                dialog.transactionId = id;
                dialog.amount = clickedItem.getString(clickedItem.getColumnIndex(TransactionContract.COLUMN_AMOUNT));
                dialog.comment = clickedItem.getString(clickedItem.getColumnIndex(TransactionContract.COLUMN_COMMENT));
                dialog.show(getSupportFragmentManager(), EDIT_TRANSACTION_DIALOG_TAG);
            }
        });

        getContentResolver().registerContentObserver(TransactionContract.CONTENT_URI, true, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange, null);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                Log.d(TAG, "Content changed. Requesting sync...");
                bus.post(new RequestSyncCommand());
            }
        });

        final int imeActionReport = getResources().getInteger(R.integer.ime_action_report);
        comment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == imeActionReport) {
                    reportNewTransaction(v);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_report, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        bus.register(this);

        Log.d(TAG, "Requesting sync on start...");
        bus.post(new RequestSyncCommand());
    }

    @Override
    protected void onStop() {
        super.onStop();
        bus.unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_synchronize) {
            RequestSyncCommand cmd = new RequestSyncCommand();
            cmd.isManual = true;
            bus.post(cmd);
            return true;
        }
        if (id == R.id.action_bank_links) {
            startActivity(new Intent(this, BankLinksActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void reportNewTransaction(View view) {
        String amountValue = amount.getText().toString();
        if (amountValue.isEmpty()) {
            Toast.makeText(this, getString(R.string.amount_is_required), Toast.LENGTH_SHORT).show();
            return;
        }
        String commentValue = comment.getText().toString();
        findViewById(R.id.report).setEnabled(false);
        String accountId = SharedPreferencesUtil.getDefaultSharedPreferences(this).getString(SettingsFragment.KEY_DEFAULT_ACCOUNT_ID, null);
        bus.post(new ReportTransactionCommand(accountId, amountValue, commentValue));
    }

    @EventHandler
    public void onEventMainThread(TransactionReportedEvent event) {
        report.setEnabled(true);
        amount.setText("");
        amount.requestFocus();
        comment.setText("");

        Toast.makeText(ReportActivity.this, getString(R.string.transaction_reported), Toast.LENGTH_SHORT).show();
    }

    @EventHandler
    public void onEventMainThread(TransactionsDeletedEvent event) {
        int removedLength = event.getIds().length;
        String message = getResources().getQuantityString(R.plurals.transactions_removed, removedLength, removedLength);
        Toast.makeText(ReportActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    protected boolean doNotCallRequestSync = false;

    @EventHandler
    public void onEvent(RequestSyncCommand cmd) {
        SharedPreferences prefs = SharedPreferencesUtil.getDefaultSharedPreferences(this);
        boolean shouldUseManualSync = prefs.getBoolean(SettingsFragment.KEY_USE_MANUAL_SYNC, false);
        if (shouldUseManualSync && !cmd.isManual) {
            Log.d(TAG, "Automatic synchronization is disabled.");
            return;
        }
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_DO_NOT_RETRY, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_BACKOFF, true);
        if (doNotCallRequestSync) {
            Log.w(TAG, "The requestSync skipped because special flag st to true. Is it a test mode?");
        } else {
            ContentResolver.requestSync(null, TransactionContract.AUTHORITY, settingsBundle);
        }
    }

    private class LoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new AsyncTaskLoader<Cursor>(ReportActivity.this) {
                @Override protected void onStartLoading() {
                    forceLoad();
                }

                @Override public Cursor loadInBackground() {
                    Log.d(TAG, "Loading transactions....");
                    final MatrixCursor cursor = new MatrixCursor(new String[]{
                            TransactionContract.COLUMN_ID, TransactionContract.COLUMN_AMOUNT, TransactionContract.COLUMN_COMMENT
                    });
                    try {
                        for (PendingTransaction transaction : transactionsReadModel.getTransactions()) {
                            cursor.addRow(new Object[] { transaction.getId(), transaction.amount, transaction.comment });
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    return cursor;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.d(TAG, "Finished loading transactions.");
            reportedTransactionsAdapter.swapCursor(data);
            bus.post(new TransactionsLoaded());
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            reportedTransactionsAdapter.swapCursor(null);
        }

    }

    private class ModeCallback implements ListView.MultiChoiceModeListener {

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.transactions_actions, menu);
            mode.setTitle(getString(R.string.select_transactions));
            setSubtitle(mode);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_delete:
                    long[] checkedItemIds = lvReportedTransactions.getCheckedItemIds();
                    bus.post(new DeleteTransactionsCommand(checkedItemIds));
                    mode.finish();
                    break;
                default:
                    throw new UnsupportedOperationException("Action item " + item.getTitle() + " is not supported.");
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
        }

        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            setSubtitle(mode);
        }

        private void setSubtitle(ActionMode mode) {
            final int checkedCount = lvReportedTransactions.getCheckedItemCount();
            if (checkedCount == 0) {
                mode.setSubtitle(null);
            } else {
                String selectedString = getResources().getQuantityString(R.plurals.number_of_selected_transactions, checkedCount, checkedCount);
                mode.setSubtitle(selectedString);
            }
        }
    }

    public static class RequestSyncCommand {
        public boolean isManual = false;
    }

    public static class TransactionsLoaded {
    }
}
