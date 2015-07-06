package com.infora.ledger;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.widget.EditText;

import com.infora.ledger.banks.ua.privatbank.PrivatBankLinkData;
import com.infora.ledger.mocks.DummyPrivatBankLinkActivity;

/**
 * Created by jenya on 01.06.15.
 */
public class PrivatBankLinkFragmentTest extends ActivityUnitTestCase<DummyPrivatBankLinkActivity> {

    public PrivatBankLinkFragmentTest() {
        super(DummyPrivatBankLinkActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        startActivity(new Intent(), null, null);
    }

    private PrivatBankLinkFragment getFragment() {
        return (PrivatBankLinkFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.bank_link_fragment);
    }

    public void testGetBankLinkData() {
        PrivatBankLinkFragment fragment = getFragment();
        EditText merchantId = (EditText) getActivity().findViewById(R.id.privat_bank_merchant_id);
        EditText merchantPassword = (EditText) getActivity().findViewById(R.id.privat_bank_merchant_password);
        EditText card = (EditText) getActivity().findViewById(R.id.privat_bank_card_number);

        merchantId.setText("merchant-100");
        merchantPassword.setText("merchant-100-password");
        card.setText("card100");

        PrivatBankLinkData linkData = fragment.getBankLinkData();
        assertEquals("merchant-100", linkData.merchantId);
        assertEquals("merchant-100-password", linkData.password);
        assertEquals("card100", linkData.card);
    }

    public void testSetBankLinkData() {
        PrivatBankLinkFragment fragment = getFragment();
        EditText merchantId = (EditText) getActivity().findViewById(R.id.privat_bank_merchant_id);
        EditText merchantPassword = (EditText) getActivity().findViewById(R.id.privat_bank_merchant_password);
        EditText card = (EditText) getActivity().findViewById(R.id.privat_bank_card_number);

        PrivatBankLinkData linkData = new PrivatBankLinkData("card100", "merchant-100", "merchant-100-password");
        fragment.setBankLinkData(linkData);

        assertEquals("merchant-100", merchantId.getText().toString());
        assertEquals("merchant-100-password", merchantPassword.getText().toString());
        assertEquals("card100", card.getText().toString());
    }

    public void testClearLinkData() {
        PrivatBankLinkFragment fragment = getFragment();
        EditText merchantId = (EditText) getActivity().findViewById(R.id.privat_bank_merchant_id);
        EditText merchantPassword = (EditText) getActivity().findViewById(R.id.privat_bank_merchant_password);
        EditText card = (EditText) getActivity().findViewById(R.id.privat_bank_card_number);

        merchantId.setText("merchant-100");
        merchantPassword.setText("merchant-100-password");
        card.setText("card100");

        fragment.clearLinkData();

        assertEquals("", merchantId.getText().toString());
        assertEquals("", merchantPassword.getText().toString());
        assertEquals("", card.getText().toString());
    }
}