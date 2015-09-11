package com.infora.ledger;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.widget.EditText;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.ua.privatbank.Privat24BankLinkData;
import com.infora.ledger.banks.ua.privatbank.PrivatBankLinkData;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.mocks.DummyBankLinkFragmentTestActivity;

/**
 * Created by jenya on 01.06.15.
 */
public class Privat24BankLinkFragmentTest extends ActivityUnitTestCase<DummyBankLinkFragmentTestActivity> {

    private Privat24BankLinkFragment fragment;
    private DeviceSecret secret;

    public Privat24BankLinkFragmentTest() {
        super(DummyBankLinkFragmentTestActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        startActivity(new Intent(getInstrumentation().getTargetContext(), DummyBankLinkFragmentTestActivity.class), null, null);
        getActivity().fragment = fragment = new Privat24BankLinkFragment();
        getInstrumentation().callActivityOnStart(getActivity());
        getActivity().getSupportFragmentManager().executePendingTransactions();
        secret = DeviceSecret.generateNew();
    }

    public void testGetBankLinkData() {
        EditText login = (EditText) fragment.getView().findViewById(R.id.privat24_login);
        EditText password = (EditText) fragment.getView().findViewById(R.id.privat24_password);
        EditText card = (EditText) fragment.getView().findViewById(R.id.privat24_card_number);

        login.setText("login-100");
        password.setText("login-100-password");
        card.setText("card100");

        Privat24BankLinkData linkData = fragment.getBankLinkData();
        assertEquals("login-100", linkData.login);
        assertEquals("login-100-password", linkData.password);
        assertEquals("card100", linkData.cardNumber);
    }

    public void testSetBankLinkData() {
        EditText login = (EditText) fragment.getView().findViewById(R.id.privat24_login);
        EditText password = (EditText) fragment.getView().findViewById(R.id.privat24_password);
        EditText card = (EditText) fragment.getView().findViewById(R.id.privat24_card_number);

        Privat24BankLinkData linkData = new Privat24BankLinkData()
                .setLogin("login-100").setPassword("login-100-password").setCardNumber("card100");
        fragment.setBankLinkData(new BankLink().setLinkData(linkData, secret), secret);

        assertEquals("login-100", login.getText().toString());
        assertEquals("login-100-password", password.getText().toString());
        assertEquals("card100", card.getText().toString());
    }

    public void testClearLinkData() {
        EditText login = (EditText) fragment.getView().findViewById(R.id.privat24_login);
        EditText password = (EditText) fragment.getView().findViewById(R.id.privat24_password);
        EditText card = (EditText) fragment.getView().findViewById(R.id.privat24_card_number);

        login.setText("login-100");
        password.setText("login-100-password");
        card.setText("card100");

        fragment.clearLinkData();

        assertEquals("", login.getText().toString());
        assertEquals("", password.getText().toString());
        assertEquals("", card.getText().toString());
    }
}