package com.infora.ledger;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by jenya on 30.05.15.
 */
public class BankLinksActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_links);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
