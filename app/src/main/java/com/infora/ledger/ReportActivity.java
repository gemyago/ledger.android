package com.infora.ledger;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.UUID;


public class ReportActivity extends ActionBarActivity {
    private static final String TAG = ReportActivity.class.getName();
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
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

    private class ReportNewTransactionTask extends AsyncTask<PendingTransaction, Void, Void> {
        @Override
        protected void onPreExecute() {
            View btnReport = findViewById(R.id.report);
            btnReport.setEnabled(false);
        }

        @Override
        protected Void doInBackground(PendingTransaction... params) {
            PendingTransactionsRepository repo = new PendingTransactionsRepository(ReportActivity.this);
            for (PendingTransaction pendingTransaction : params) {
                Log.d(TAG, "Reporting amount: " + pendingTransaction.getAmount() + ", " + pendingTransaction.getComment());
                repo.save(pendingTransaction);
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
