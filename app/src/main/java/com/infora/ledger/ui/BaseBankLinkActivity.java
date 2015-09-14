package com.infora.ledger.ui;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.infora.ledger.AddBankLinkActivity;
import com.infora.ledger.R;

/**
 * Created by mye on 9/14/2015.
 */
public abstract class BaseBankLinkActivity extends AppCompatActivity {
    protected BankLinkFragment bankLinkFragment;
    private BankLinkFragment.Mode mode;

    protected BaseBankLinkActivity(BankLinkFragment.Mode mode) {
        this.mode = mode;
    }

    protected <TFragment extends BankLinkFragment> void setBankLinkFragment(TFragment fragment) {
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        TFragment oldFragment = (TFragment) getSupportFragmentManager().findFragmentByTag(AddBankLinkActivity.BANK_LINK_FRAGMENT);
        if (oldFragment != null) {
            oldFragment.onBeforeRemove(this);
            t.remove(oldFragment);
        }
        if (fragment != null) {
            fragment.setMode(mode);
            fragment.onBeforeAdd(this);
            t.replace(R.id.bank_link_fragment_container, fragment, AddBankLinkActivity.BANK_LINK_FRAGMENT);
        }
        t.commit();
        bankLinkFragment = fragment;
    }
}
