package com.infora.ledger;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by jenya on 31.05.15.
 */
public class AddBankLinkActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bank_link);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
