package com.infora.ledger.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.data.BankLink;

/**
 * Created by mye on 7/7/2015.
 */
public abstract class BankLinkFragment<TLinkData> extends Fragment {
    private BankLink assignedBankLink;
    private DeviceSecret assignedSecret;

    public abstract TLinkData getBankLinkData();

    private boolean isViewCreated;

    public void onBeforeAdd(Activity parent) {

    }

    public void onBeforeRemove(Activity parent) {

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewCreated = true;
        if (assignedBankLink != null) {
            assignValues(assignedBankLink, assignedSecret);
            assignedBankLink = null;
            assignedSecret = null;
        }
    }

    public void setBankLinkData(BankLink bankLink, DeviceSecret secret) {
        if(isViewCreated) assignValues(bankLink, secret);
        else {
            assignedBankLink = bankLink;
            assignedSecret = secret;
        }
    }

    public abstract void clearLinkData();

    protected abstract void assignValues(BankLink bankLink, DeviceSecret secret);
}
