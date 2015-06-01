package com.infora.ledger;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.infora.ledger.BanksContract.BankLinks;

/**
 * Created by jenya on 30.05.15.
 */
public class BankLinksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int BANK_LINKS_LOADER_ID = 1;
    private SimpleCursorAdapter bankLinksAdapter;
    private ListView lvBankLinks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_links);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bankLinksAdapter = new SimpleCursorAdapter(this, R.layout.banks_links_list,
                null,
                new String[]{BankLinks.COLUMN_BIC, BankLinks.COLUMN_ACCOUNT_NAME},
                new int[]{R.id.bank_link_data, R.id.ledger_account_name}, 0);

        lvBankLinks = (ListView) findViewById(R.id.bank_links_list);
        lvBankLinks.setAdapter(bankLinksAdapter);
        lvBankLinks.setEmptyView(findViewById(android.R.id.empty));

        getLoaderManager().initLoader(BANK_LINKS_LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bank_links, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_bank_link) {
            startActivity(new Intent(this, AddBankLinkActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (BANK_LINKS_LOADER_ID == id) {
            return new CursorLoader(this, BankLinks.CONTENT_URI, null, null, null, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        bankLinksAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bankLinksAdapter.swapCursor(null);
    }
}
