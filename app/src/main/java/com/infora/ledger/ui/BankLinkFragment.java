package com.infora.ledger.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.infora.ledger.data.BankLink;

/**
 * Created by mye on 7/7/2015.
 */
public abstract class BankLinkFragment<TLinkData> extends Fragment {
    private BankLink assignedBankLink;

    public abstract TLinkData getBankLinkData();

    private boolean isViewCreated;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewCreated = true;
        if (assignedBankLink != null) {
            assignValues(assignedBankLink);
            assignedBankLink = null;
        }
    }

    public void setBankLinkData(BankLink bankLink) {
        if(isViewCreated) assignValues(bankLink);
        else assignedBankLink = bankLink;
    }

    public abstract void clearLinkData();

    protected abstract void assignValues(BankLink bankLink);
}
