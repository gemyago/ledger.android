package com.infora.ledger;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.infora.ledger.banks.ua.privatbank.PrivatBankLinkData;
import com.infora.ledger.banks.ua.urksibbank.UrksibBankLinkData;
import com.infora.ledger.ui.BankLinkFragment;

/**
 * Created by jenya on 01.06.15.
 */
public class UkrsibBankLinkFragment extends Fragment implements BankLinkFragment<UrksibBankLinkData> {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ukrsib_bank_link, container, false);
    }

    @Override
    public UrksibBankLinkData getBankLinkData() {
        return null;
    }

    @Override
    public void setBankLinkData(UrksibBankLinkData linkData) {

    }

    @Override
    public void clearLinkData() {
    }
}
