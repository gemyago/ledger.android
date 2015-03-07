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
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ReportActivity extends ActionBarActivity {
    private static final int REPORTED_TRANSACTIONS_LOADER_ID = 1;
    private static final String TAG = ReportActivity.class.getName();
    private SimpleCursorAdapter reportedTransactionsAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        reportedTransactionsAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2,
                null,
                new String[]{PendingTransactionContract.COLUMN_AMOUNT, PendingTransactionContract.COLUMN_COMMENT},
                new int[]{android.R.id.text1, android.R.id.text2}, 0);
        ListView lvReportedTransactions = (ListView) findViewById(R.id.reported_transactions_list);
        lvReportedTransactions.setAdapter(reportedTransactionsAdapter);
        getLoaderManager().initLoader(REPORTED_TRANSACTIONS_LOADER_ID, null, new LoaderCallbacks());
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
        new ReportNewTransactionTask().execute(PendingTransaction.appendValues(new ContentValues(), amount, comment));
    }

    private class LoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(ReportActivity.this,
                    PendingTransactionContract.CONTENT_URI,
                    null,
                    null,
                    null,
                    PendingTransactionContract.COLUMN_TIMESTAMP + " DESC");
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

    private class ReportNewTransactionTask extends AsyncTask<ContentValues, Void, Void> {
        @Override
        protected void onPreExecute() {
            View btnReport = findViewById(R.id.report);
            btnReport.setEnabled(false);
        }

        @Override
        protected Void doInBackground(ContentValues... params) {
            for (ContentValues pendingTransaction : params) {
                Log.d(TAG, "Reporting new transaction");
                getContentResolver().insert(PendingTransactionContract.CONTENT_URI, pendingTransaction);
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
        }
    }
}
