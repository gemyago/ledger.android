package com.infora.ledger;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.infora.ledger.application.commands.Command;
import com.infora.ledger.application.commands.DeleteTransactionsCommand;
import com.infora.ledger.application.commands.ReportTransactionCommand;
import com.infora.ledger.application.di.DiUtils;
import com.infora.ledger.application.events.SynchronizationCompleted;
import com.infora.ledger.application.events.SynchronizationFailed;
import com.infora.ledger.application.events.TransactionAdjusted;
import com.infora.ledger.application.events.TransactionReportedEvent;
import com.infora.ledger.application.events.TransactionsDeletedEvent;
import com.infora.ledger.application.synchronization.SynchronizationStrategiesFactory;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.data.TransactionsReadModel;
import com.infora.ledger.support.EventHandler;
import com.infora.ledger.support.SharedPreferencesProvider;
import com.infora.ledger.support.SyncService;

import java.sql.SQLException;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

import static com.infora.ledger.application.synchronization.SynchronizationStrategiesFactory.OPTION_SYNC_SINGLE_TRANSACTION;
import static com.infora.ledger.application.synchronization.SynchronizationStrategiesFactory.OPTION_SYNC_SINGLE_TRANSACTION_ACTION;
import static com.infora.ledger.application.synchronization.SynchronizationStrategiesFactory.SYNC_ACTION_ADJUST;
import static com.infora.ledger.application.synchronization.SynchronizationStrategiesFactory.SYNC_ACTION_PUBLISH;
import static com.infora.ledger.application.synchronization.SynchronizationStrategiesFactory.SYNC_ACTION_REJECT;

public class ReportActivity extends AppCompatActivity {
    private static final String TAG = ReportActivity.class.getName();
    private static final int REPORTED_TRANSACTIONS_LOADER_ID = 1;
    private SimpleCursorAdapter reportedTransactionsAdapter;

    @Bind(R.id.reported_transactions_list) ListView lvReportedTransactions;
    @Bind(R.id.comment) EditText comment;
    @Bind(R.id.amount) EditText amount;
    @Bind(R.id.report) Button report;
    @Bind(R.id.swipe_refresh) SwipeRefreshLayout swipeRefresh;

    @Inject EventBus bus;
    @Inject TransactionsReadModel transactionsReadModel;
    @Inject SyncService syncService;
    @Inject SharedPreferencesProvider prefsProvider;

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
                Cursor clickedItem = (Cursor) lvReportedTransactions.getItemAtPosition(position);
                dialog.transactionId = id;
                dialog.amount = clickedItem.getString(clickedItem.getColumnIndex(TransactionContract.COLUMN_AMOUNT));
                dialog.comment = clickedItem.getString(clickedItem.getColumnIndex(TransactionContract.COLUMN_COMMENT));
                dialog.show(getSupportFragmentManager(), EDIT_TRANSACTION_DIALOG_TAG);
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

        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override public void onRefresh() {
                onEvent(new RequestSyncCommand().setManual(true));
            }
        });

        lvReportedTransactions.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (lvReportedTransactions == null || lvReportedTransactions.getChildCount() == 0) ?
                        0 : lvReportedTransactions.getChildAt(0).getTop();
                swipeRefresh.setEnabled((topRowVerticalPosition >= 0));
            }
        });
    }

    public void onEventMainThread(SynchronizationCompleted event) {
        Log.d(TAG, "The synchronization has been completed. Clearing refreshing flag...");
        swipeRefresh.setRefreshing(false);
    }

    public void onEventMainThread(SynchronizationFailed event) {
        Log.d(TAG, "The synchronization has been failed. Clearing refreshing flag...");
        swipeRefresh.setRefreshing(false);
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
        bus.post(new RequestSyncCommand().setLedgerWebOnly(true));
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
            bus.post(new RequestSyncCommand().setManual(true));
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
        String accountId = SharedPreferencesProvider.getDefaultSharedPreferences(this).getString(SettingsFragment.KEY_DEFAULT_ACCOUNT_ID, null);
        bus.post(new ReportTransactionCommand(accountId, amountValue, commentValue));
    }

    @EventHandler
    public void onEventMainThread(TransactionReportedEvent event) {
        report.setEnabled(true);
        amount.setText("");
        amount.requestFocus();
        comment.setText("");

        Toast.makeText(ReportActivity.this, getString(R.string.transaction_reported), Toast.LENGTH_SHORT).show();

        restartTransactionsLoader();
        Bundle syncOptions = new Bundle();
        syncOptions.putInt(OPTION_SYNC_SINGLE_TRANSACTION, (int) event.getId());
        syncOptions.putString(OPTION_SYNC_SINGLE_TRANSACTION_ACTION, SYNC_ACTION_PUBLISH);
        doForceSync(syncOptions);
    }

    public void onEventMainThread(TransactionAdjusted event) {
        restartTransactionsLoader();
        Bundle syncOptions = new Bundle();
        syncOptions.putInt(OPTION_SYNC_SINGLE_TRANSACTION, (int) event.id);
        syncOptions.putString(OPTION_SYNC_SINGLE_TRANSACTION_ACTION, SYNC_ACTION_ADJUST);
        doForceSync(syncOptions);
    }

    @EventHandler
    public void onEventMainThread(TransactionsDeletedEvent event) {
        int removedLength = event.getIds().length;
        String message = getResources().getQuantityString(R.plurals.transactions_removed, removedLength, removedLength);
        Toast.makeText(ReportActivity.this, message, Toast.LENGTH_SHORT).show();
        restartTransactionsLoader();

        for (long id: event.getIds()) {
            Bundle syncOptions = new Bundle();
            syncOptions.putInt(OPTION_SYNC_SINGLE_TRANSACTION, (int) id);
            syncOptions.putString(OPTION_SYNC_SINGLE_TRANSACTION_ACTION, SYNC_ACTION_REJECT);
            doForceSync(syncOptions);
        }
    }

    private void restartTransactionsLoader() {
        getLoaderManager().restartLoader(REPORTED_TRANSACTIONS_LOADER_ID, null, new LoaderCallbacks());
    }

    @EventHandler
    public void onEvent(RequestSyncCommand cmd) {
        if (prefsProvider.useManualSync() && !cmd.isManual) {
            Log.d(TAG, "Automatic synchronization is disabled.");
            return;
        }
        if(!swipeRefresh.isRefreshing()) swipeRefresh.setRefreshing(true);
        Bundle settingsBundle = new Bundle();
        if(cmd.isLedgerWebOnly) {
            Log.d(TAG, "Requesting synchronization with ledger web only.");
            settingsBundle.putBoolean(SynchronizationStrategiesFactory.OPTION_SYNCHRONIZE_LEDGER_WEB, true);
        }

        doForceSync(settingsBundle);
    }

    private void doForceSync(Bundle settingsBundle) {
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_DO_NOT_RETRY, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_BACKOFF, true);
        syncService.requestSync(null, TransactionContract.AUTHORITY, settingsBundle);
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
                            cursor.addRow(new Object[]{transaction.getId(), transaction.amount, transaction.comment});
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

    public static class RequestSyncCommand extends Command {
        public boolean isManual = false;
        public boolean isLedgerWebOnly = false;

        public RequestSyncCommand setManual(boolean value) {
            isManual = value;
            return this;
        }

        public RequestSyncCommand setLedgerWebOnly(boolean ledgerWebOnly) {
            this.isLedgerWebOnly = ledgerWebOnly;
            return this;
        }
    }

    public static class TransactionsLoaded {
    }
}
