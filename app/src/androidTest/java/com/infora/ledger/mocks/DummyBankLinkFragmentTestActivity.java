package com.infora.ledger.mocks;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.infora.ledger.PrivatBankLinkFragment;

/**
 * Created by jenya on 04.07.15.
 */
public class DummyBankLinkFragmentTestActivity extends FragmentActivity {

    public PrivatBankLinkFragment fragment;

    @Override
    protected void onStart() {
        super.onStart();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(fragment, "test-fragment");
        transaction.commit();
    }
}
