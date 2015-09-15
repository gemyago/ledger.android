package com.infora.ledger;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.ua.urksibbank.UkrsibBankLinkData;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.ui.BankLinkFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by jenya on 01.06.15.
 */
public class UkrsibBankLinkFragment extends BankLinkFragment<UkrsibBankLinkData> {

    @Bind(R.id.ukrsib_bank_login)
    EditText login;
    @Bind(R.id.ukrsib_bank_password)
    EditText password;
    @Bind(R.id.ukrsib_bank_account_number)
    EditText account;
    @Bind(R.id.ukrsib_bank_fetch_account_transactions)
    CheckBox fetchAccountTransactions;
    @Bind(R.id.ukrsib_bank_card_number)
    EditText card;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ukrsib_bank_link, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public UkrsibBankLinkData getBankLinkData() {
        return new UkrsibBankLinkData(
                login.getText().toString(),
                password.getText().toString(),
                account.getText().toString(),
                card.getText().toString(),
                fetchAccountTransactions.isChecked());
    }

    @Override
    public void assignValues(BankLink bankLink, DeviceSecret secret) {
        UkrsibBankLinkData linkData = bankLink.getLinkData(UkrsibBankLinkData.class, secret);
        login.setText(linkData.login);
        password.setText(linkData.password);
        account.setText(linkData.account);
        fetchAccountTransactions.setChecked(linkData.fetchAccountTransactions);
        card.setText(linkData.card);
    }

    @Override
    public void clearLinkData() {
        login.setText("");
        password.setText("");
        account.setText("");
        fetchAccountTransactions.setChecked(false);
        card.setText("");
    }
}
