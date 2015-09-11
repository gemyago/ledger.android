package com.infora.ledger;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.ua.privatbank.Privat24BankLinkData;
import com.infora.ledger.banks.ua.privatbank.PrivatBankLinkData;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.ui.BankLinkFragment;

/**
 * Created by jenya on 01.06.15.
 */
public class Privat24BankLinkFragment extends BankLinkFragment<Privat24BankLinkData> {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_privat24_bank_link, container, false);
    }

    @Override
    public Privat24BankLinkData getBankLinkData() {
        EditText login = (EditText) getView().findViewById(R.id.privat24_login);
        EditText password = (EditText) getView().findViewById(R.id.privat24_password);
        EditText card = (EditText) getView().findViewById(R.id.privat24_card_number);
        return new Privat24BankLinkData()
                .setLogin(login.getText().toString())
                .setPassword(password.getText().toString())
                .setCardNumber(card.getText().toString());
    }

    @Override
    public void assignValues(BankLink bankLink, DeviceSecret secret) {
        EditText login = (EditText) getView().findViewById(R.id.privat24_login);
        EditText password = (EditText) getView().findViewById(R.id.privat24_password);
        EditText card = (EditText) getView().findViewById(R.id.privat24_card_number);
        Privat24BankLinkData linkData = bankLink.getLinkData(Privat24BankLinkData.class, secret);
        login.setText(linkData.login);
        password.setText(linkData.password);
        card.setText(linkData.cardNumber);
    }

    @Override
    public void clearLinkData() {
        EditText login = (EditText) getView().findViewById(R.id.privat24_login);
        EditText password = (EditText) getView().findViewById(R.id.privat24_password);
        EditText card = (EditText) getView().findViewById(R.id.privat24_card_number);
        login.setText("");
        password.setText("");
        card.setText("");
    }
}
