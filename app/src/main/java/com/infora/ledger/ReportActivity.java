package com.infora.ledger;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.infora.ledger.application.commands.DeleteTransactionsCommand;
import com.infora.ledger.application.commands.ReportTransactionCommand;
import com.infora.ledger.application.events.TransactionReportedEvent;
import com.infora.ledger.application.events.TransactionsDeletedEvent;
import com.infora.ledger.support.BusUtils;
import com.infora.ledger.support.EventHandler;
import com.infora.ledger.support.SharedPreferencesUtil;

public class ReportActivity extends ActionBarActivity {
    private static final String TAG = ReportActivity.class.getName();
    private static final int REPORTED_TRANSACTIONS_LOADER_ID = 1;
    private SimpleCursorAdapter reportedTransactionsAdapter;
    private ListView lvReportedTransactions;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        reportedTransactionsAdapter = new SimpleCursorAdapter(this, R.layout.transactions_list,
                null,
                new String[]{TransactionContract.COLUMN_AMOUNT, TransactionContract.COLUMN_COMMENT},
                new int[]{R.id.amount, R.id.comment}, 0);
        lvReportedTransactions = (ListView) findViewById(R.id.reported_transactions_list);
        lvReportedTransactions.setAdapter(reportedTransactionsAdapter);
        getLoaderManager().initLoader(REPORTED_TRANSACTIONS_LOADER_ID, null, new LoaderCallbacks());

        lvReportedTransactions.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lvReportedTransactions.setMultiChoiceModeListener(new ModeCallback());

        getContentResolver().registerContentObserver(TransactionContract.CONTENT_URI, true, new ContentObserver(null) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange, null);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                Log.d(TAG, "Content changed. Requesting sync...");
                BusUtils.post(ReportActivity.this, new RequestSyncCommand());
            }
        });

        EditText comment = (EditText) findViewById(R.id.comment);
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
        BusUtils.register(this);

        Log.d(TAG, "Requesting sync on start...");
        BusUtils.post(this, new RequestSyncCommand());
    }

    @Override
    protected void onStop() {
        super.onStop();
        BusUtils.unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_synchronize) {
            RequestSyncCommand cmd = new RequestSyncCommand();
            cmd.isManual = true;
            BusUtils.post(this, cmd);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    public void reportNewTransaction(View view) {
        EditText etAmount = ((EditText) findViewById(R.id.amount));
        EditText etComment = ((EditText) findViewById(R.id.comment));
        String amount = etAmount.getText().toString();
        if (amount.isEmpty()) {
            Toast.makeText(this, getString(R.string.amount_is_required), Toast.LENGTH_SHORT).show();
            return;
        }
        String comment = etComment.getText().toString();
        findViewById(R.id.report).setEnabled(false);
        BusUtils.post(this, new ReportTransactionCommand(amount, comment));
    }

    @EventHandler
    public void onEventMainThread(TransactionReportedEvent event) {
        View btnReport = findViewById(R.id.report);
        EditText etAmount = ((EditText) findViewById(R.id.amount));
        EditText etComment = ((EditText) findViewById(R.id.comment));

        btnReport.setEnabled(true);
        etAmount.setText("");
        etAmount.requestFocus();
        etComment.setText("");

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
        if(doNotCallRequestSync) {
            Log.w(TAG, "The requestSync skipped because special flag st to true. Is it a test mode?");
        } else {
            ContentResolver.requestSync(null, TransactionContract.AUTHORITY, settingsBundle);
        }
    }

    private class LoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(ReportActivity.this,
                    TransactionContract.CONTENT_URI,
                    null,
                    TransactionContract.COLUMN_IS_DELETED + " = 0",
                    null,
                    TransactionContract.COLUMN_TIMESTAMP + " DESC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            reportedTransactionsAdapter.swapCursor(data);
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
                    BusUtils.post(ReportActivity.this, new DeleteTransactionsCommand(checkedItemIds));
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
}
