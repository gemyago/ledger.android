package com.infora.ledger;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.infora.ledger.banks.ua.urksibbank.UkrsibBankLinkData;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.ui.BankLinkFragment;

/**
 * Created by jenya on 01.06.15.
 */
public class UkrsibBankLinkFragment extends BankLinkFragment<UkrsibBankLinkData> {

    private EditText login;
    private EditText password;
    private EditText card;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ukrsib_bank_link, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        login = (EditText) getView().findViewById(R.id.ukrsib_bank_login);
        password = (EditText) getView().findViewById(R.id.ukrsib_bank_password);
        card = (EditText) getView().findViewById(R.id.ukrsib_bank_card_number);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public UkrsibBankLinkData getBankLinkData() {
        return new UkrsibBankLinkData(login.getText().toString(), password.getText().toString(), null, card.getText().toString(), false);
    }

    @Override
    public void assignValues(BankLink bankLink) {
        UkrsibBankLinkData linkData = bankLink.getLinkData(UkrsibBankLinkData.class);
        login.setText(linkData.login);
        password.setText(linkData.password);
        card.setText(linkData.card);
    }

    @Override
    public void clearLinkData() {
        login.setText("");
        password.setText("");
        card.setText("");
    }
}
