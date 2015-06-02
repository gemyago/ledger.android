package com.infora.ledger;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;

import com.infora.ledger.banks.PrivatBankLinkData;

/**
 * Created by jenya on 01.06.15.
 */
public class PrivatBankLinkFragmentTest extends ActivityInstrumentationTestCase2<AddBankLinkActivity> {
    public PrivatBankLinkFragmentTest() {
        super(AddBankLinkActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    private PrivatBankLinkFragment startFragment() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        PrivatBankLinkFragment fragment = new PrivatBankLinkFragment();
        transaction.add(fragment, "tag");
        transaction.commit();
        getInstrumentation().waitForIdleSync();
        return fragment;
    }

    public void testGetBankLinkData() {
        PrivatBankLinkFragment fragment = startFragment();
        EditText merchantId = (EditText) fragment.getView().findViewById(R.id.privat_bank_merchant_id);
        EditText merchantPassword = (EditText) fragment.getView().findViewById(R.id.privat_bank_merchant_password);
        EditText card = (EditText) fragment.getView().findViewById(R.id.privat_bank_card_number);

        merchantId.setText("merchant-100");
        merchantPassword.setText("merchant-100-password");
        card.setText("card100");

        PrivatBankLinkData linkData = fragment.getBankLinkData();
        assertEquals("merchant-100", linkData.merchantId);
        assertEquals("merchant-100-password", linkData.password);
        assertEquals("card100", linkData.card);
    }

    public void testSetBankLinkData() {
        PrivatBankLinkFragment fragment = startFragment();
        EditText merchantId = (EditText) fragment.getView().findViewById(R.id.privat_bank_merchant_id);
        EditText merchantPassword = (EditText) fragment.getView().findViewById(R.id.privat_bank_merchant_password);
        EditText card = (EditText) fragment.getView().findViewById(R.id.privat_bank_card_number);

        PrivatBankLinkData linkData = new PrivatBankLinkData("card100", "merchant-100", "merchant-100-password");
        fragment.setBankLinkData(linkData);

        assertEquals("merchant-100", merchantId.getText().toString());
        assertEquals("merchant-100-password", merchantPassword.getText().toString());
        assertEquals("card100", card.getText().toString());
    }

    public void testClearLinkData() {
        PrivatBankLinkFragment fragment = startFragment();
        EditText merchantId = (EditText) fragment.getView().findViewById(R.id.privat_bank_merchant_id);
        EditText merchantPassword = (EditText) fragment.getView().findViewById(R.id.privat_bank_merchant_password);
        EditText card = (EditText) fragment.getView().findViewById(R.id.privat_bank_card_number);

        merchantId.setText("merchant-100");
        merchantPassword.setText("merchant-100-password");
        card.setText("card100");

        fragment.clearLinkData();

        assertEquals("", merchantId.getText().toString());
        assertEquals("", merchantPassword.getText().toString());
        assertEquals("", card.getText().toString());
    }
}