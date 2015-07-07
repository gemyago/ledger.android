package com.infora.ledger;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.infora.ledger.banks.ua.privatbank.PrivatBankLinkData;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.ui.BankLinkFragment;

/**
 * Created by jenya on 01.06.15.
 */
public class PrivatBankLinkFragment extends BankLinkFragment<PrivatBankLinkData> {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_privat_bank_link, container, false);
    }

    @Override
    public PrivatBankLinkData getBankLinkData() {
        EditText merchantId = (EditText) getView().findViewById(R.id.privat_bank_merchant_id);
        EditText merchantPassword = (EditText) getView().findViewById(R.id.privat_bank_merchant_password);
        EditText card = (EditText) getView().findViewById(R.id.privat_bank_card_number);
        return new PrivatBankLinkData(card.getText().toString(), merchantId.getText().toString(), merchantPassword.getText().toString());
    }

    @Override
    public void setBankLinkData(BankLink bankLink) {
        PrivatBankLinkData linkData = bankLink.getLinkData(PrivatBankLinkData.class);
        EditText merchantId = (EditText) getView().findViewById(R.id.privat_bank_merchant_id);
        EditText merchantPassword = (EditText) getView().findViewById(R.id.privat_bank_merchant_password);
        EditText card = (EditText) getView().findViewById(R.id.privat_bank_card_number);
        merchantId.setText(linkData.merchantId);
        merchantPassword.setText(linkData.password);
        card.setText(linkData.card);
    }

    @Override
    public void clearLinkData() {
        EditText merchantId = (EditText) getView().findViewById(R.id.privat_bank_merchant_id);
        EditText merchantPassword = (EditText) getView().findViewById(R.id.privat_bank_merchant_password);
        EditText card = (EditText) getView().findViewById(R.id.privat_bank_card_number);

        merchantId.setText("");
        merchantPassword.setText("");
        card.setText("");
    }
}
