package com.infora.ledger;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.widget.EditText;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.ua.privatbank.Privat24BankLinkData;
import com.infora.ledger.banks.ua.privatbank.PrivatBankLinkData;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.mocks.DummyBankLinkFragmentTestActivity;
import com.infora.ledger.support.ObfuscatedString;
import com.infora.ledger.ui.BankLinkFragment;

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
        fragment.setMode(BankLinkFragment.Mode.Edit);
        getInstrumentation().callActivityOnStart(getActivity());
        getActivity().getSupportFragmentManager().executePendingTransactions();
        secret = DeviceSecret.generateNew();
    }

    public void testGetBankLinkData() {
        Privat24BankLinkData linkData = new Privat24BankLinkData()
                .setUniqueId("uid-100")
                .setLogin("login-100")
                .setPassword("login-100-password")
                .setCardNumber("card100")
                .setCardid("card-100");
        fragment.setBankLinkData(new BankLink().setLinkData(linkData, secret), secret);

        Privat24BankLinkData actualLinkData = fragment.getBankLinkData();
        assertEquals("uid-100", actualLinkData.uniqueId);
        assertEquals("login-100", actualLinkData.login);
        assertEquals("login-100-password", actualLinkData.password);
        assertEquals("card-100", actualLinkData.cardid);
        assertEquals("card100", actualLinkData.cardNumber);
    }

    public void testSetBankLinkData() {
        EditText login = (EditText) fragment.getView().findViewById(R.id.privat24_login);
        EditText password = (EditText) fragment.getView().findViewById(R.id.privat24_password);
        EditText card = (EditText) fragment.getView().findViewById(R.id.privat24_card_number);

        Privat24BankLinkData linkData = new Privat24BankLinkData()
                .setLogin("login-100").setPassword("login-100-password").setCardNumber("card100");
        fragment.setBankLinkData(new BankLink().setLinkData(linkData, secret), secret);

        assertFalse(login.isEnabled());
        assertFalse(password.isEnabled());
        assertFalse(card.isEnabled());

        assertEquals("login-100", login.getText().toString());
        assertEquals(ObfuscatedString.value("login-100-password"), ObfuscatedString.value(password.getText().toString()));
        assertEquals(ObfuscatedString.value("card100"), ObfuscatedString.value(card.getText().toString()));
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