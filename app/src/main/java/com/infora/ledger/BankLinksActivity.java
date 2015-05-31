package com.infora.ledger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.infora.ledger.data.BankLink;
import com.infora.ledger.support.BusUtils;

/**
 * Created by jenya on 30.05.15.
 */
public class BankLinksActivity extends ActionBarActivity {
    private SimpleCursorAdapter bankLinksAdapter;
    private ListView lvBankLinks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_links);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bankLinksAdapter = new SimpleCursorAdapter(this, R.layout.transactions_list,
                null,
                new String[]{BanksContract.BankLinks.COLUMN_BIC},
                new int[]{R.id.bank_link_data}, 0);
        lvBankLinks = (ListView) findViewById(R.id.bank_links_list);
        lvBankLinks.setAdapter(bankLinksAdapter);
        lvBankLinks.setEmptyView(findViewById(android.R.id.empty));
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
}
