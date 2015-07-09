package com.infora.ledger;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.widget.EditText;

import com.infora.ledger.banks.ua.urksibbank.UkrsibBankLinkData;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.mocks.DummyBankLinkFragmentTestActivity;

/**
 * Created by mye on 7/7/2015.
 */
public class UkrsibBankLinkFragmentTest extends ActivityUnitTestCase<DummyBankLinkFragmentTestActivity> {
    private UkrsibBankLinkFragment fragment;

    public UkrsibBankLinkFragmentTest() {
        super(DummyBankLinkFragmentTestActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        startActivity(new Intent(getInstrumentation().getTargetContext(), DummyBankLinkFragmentTestActivity.class), null, null);
        getActivity().fragment = fragment = new UkrsibBankLinkFragment();
        getInstrumentation().callActivityOnStart(getActivity());
        getActivity().getSupportFragmentManager().executePendingTransactions();
    }

    public void testSetBankLinkData() {
        EditText login = (EditText) fragment.getView().findViewById(R.id.ukrsib_bank_login);
        EditText password = (EditText) fragment.getView().findViewById(R.id.ukrsib_bank_password);
        EditText card = (EditText) fragment.getView().findViewById(R.id.ukrsib_bank_card_number);

        UkrsibBankLinkData linkData = new UkrsibBankLinkData("login-1", "password-1", null, "card-1");
        fragment.setBankLinkData(new BankLink().setLinkData(linkData));

        assertEquals(linkData.login, login.getText().toString());
        assertEquals(linkData.password, password.getText().toString());
        assertEquals(linkData.card, card.getText().toString());
    }

    public void testGetBankLinkData() {
        EditText login = (EditText) fragment.getView().findViewById(R.id.ukrsib_bank_login);
        EditText password = (EditText) fragment.getView().findViewById(R.id.ukrsib_bank_password);
        EditText card = (EditText) fragment.getView().findViewById(R.id.ukrsib_bank_card_number);

        login.setText("login-1");
        password.setText("password-1");
        card.setText("card-1");

        UkrsibBankLinkData linkData = fragment.getBankLinkData();
        assertEquals("login-1", linkData.login);
        assertEquals("password-1", linkData.password);
        assertEquals("card-1", linkData.card);
    }

    public void testClearLinkData() {
        EditText login = (EditText) fragment.getView().findViewById(R.id.ukrsib_bank_login);
        EditText password = (EditText) fragment.getView().findViewById(R.id.ukrsib_bank_password);
        EditText card = (EditText) fragment.getView().findViewById(R.id.ukrsib_bank_card_number);

        UkrsibBankLinkData linkData = new UkrsibBankLinkData("login-1", "password-1", null, "card-1");
        fragment.setBankLinkData(new BankLink().setLinkData(linkData));
        fragment.clearLinkData();

        assertEquals("", login.getText().toString());
        assertEquals("", password.getText().toString());
        assertEquals("", card.getText().toString());
    }
}
