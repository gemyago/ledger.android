package com.infora.ledger;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.UUID;


public class ReportActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int REPORTED_TRANSACTIONS_LOADER_ID = 1;
    private static final String TAG = ReportActivity.class.getName();
    private SimpleCursorAdapter reportedTransactionsAdapter;
    private LedgerDbHelper dbHelper;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        reportedTransactionsAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2,
                null,
                new String[]{PendingTransactionContract.COLUMN_AMOUNT, PendingTransactionContract.COLUMN_COMMENT},
                new int[]{android.R.id.text1, android.R.id.text2});
        ListView reportedTransactionsList = (ListView) findViewById(R.id.reported_transactions_list);
        reportedTransactionsList.setAdapter(reportedTransactionsAdapter);

        getLoaderManager().initLoader(REPORTED_TRANSACTIONS_LOADER_ID, null, this);

        dbHelper = new LedgerDbHelper(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_report, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void reportNewTransaction(View view) {
        EditText etAmount = ((EditText) findViewById(R.id.amount));
        EditText etComment = ((EditText) findViewById(R.id.comment));
        String amount = etAmount.getText().toString();
        String comment = etComment.getText().toString();
        PendingTransaction pendingTransaction = new PendingTransaction(UUID.randomUUID().toString(), amount, comment);
        new ReportNewTransactionTask().execute(pendingTransaction);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                PendingTransactionContract.CONTENT_URI,
                PendingTransactionContract.ALL_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        reportedTransactionsAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        reportedTransactionsAdapter.swapCursor(null);
    }

    private class ReportNewTransactionTask extends AsyncTask<PendingTransaction, Void, Void> {
        @Override
        protected void onPreExecute() {
            View btnReport = findViewById(R.id.report);
            btnReport.setEnabled(false);
        }

        @Override
        protected Void doInBackground(PendingTransaction... params) {
            for (PendingTransaction pendingTransaction : params) {
                Log.d(TAG, "Reporting amount: " + pendingTransaction.getAmount() + ", " + pendingTransaction.getComment());
                ContentValues values = new ContentValues();
                values.put(PendingTransactionContract.COLUMN_ID, pendingTransaction.getId());
                values.put(PendingTransactionContract.COLUMN_AMOUNT, pendingTransaction.getAmount());
                values.put(PendingTransactionContract.COLUMN_COMMENT, pendingTransaction.getComment());
                getContentResolver().insert(PendingTransactionContract.CONTENT_URI, values);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            View btnReport = findViewById(R.id.report);
            EditText etAmount = ((EditText) findViewById(R.id.amount));
            EditText etComment = ((EditText) findViewById(R.id.comment));

            btnReport.setEnabled(true);
            etAmount.setText("");
            etAmount.requestFocus();
            etComment.setText("");

            Toast.makeText(ReportActivity.this, getString(R.string.transaction_reported), Toast.LENGTH_SHORT).show();

            getLoaderManager().restartLoader(REPORTED_TRANSACTIONS_LOADER_ID, null, ReportActivity.this);
        }
    }
}
