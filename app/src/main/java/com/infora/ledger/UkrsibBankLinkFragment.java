package com.infora.ledger;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.infora.ledger.banks.ua.urksibbank.UkrsibBankLinkData;
import com.infora.ledger.ui.BankLinkFragment;

/**
 * Created by jenya on 01.06.15.
 */
public class UkrsibBankLinkFragment extends BankLinkFragment<UkrsibBankLinkData> {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ukrsib_bank_link, container, false);
    }

    @Override
    public UkrsibBankLinkData getBankLinkData() {
        return null;
    }

    @Override
    public void setBankLinkData(UkrsibBankLinkData linkData) {

    }

    @Override
    public void clearLinkData() {
    }
}
