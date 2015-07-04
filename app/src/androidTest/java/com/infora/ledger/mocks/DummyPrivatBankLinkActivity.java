package com.infora.ledger.mocks;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.infora.ledger.PrivatBankLinkFragment;
import com.infora.ledger.R;

/**
 * Created by jenya on 04.07.15.
 */
public class DummyPrivatBankLinkActivity extends FragmentActivity {

    public PrivatBankLinkFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bank_link);
    }
}
