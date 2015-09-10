package com.infora.ledger.banks.ua.privatbank;

import android.test.AndroidTestCase;

import com.infora.ledger.support.LogUtil;

import java.io.IOException;

/**
 * Created by mye on 9/10/2015.
 */
public class Privat24ApiManualTest extends AndroidTestCase {
    private Privat24Api api;
    private Privat24BankLinkData linkData;

    @Override
    protected void runTest() throws Throwable {
        boolean shouldRun = false;
        shouldRun = true; //Uncomment this line to run tests
        if (shouldRun) {
            super.runTest();
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        linkData = new Privat24BankLinkData()
                .setLogin("TODO")
                .setPassword("TODO")
                .setCookie("TODO: Set from logs after testAuthenticateWithOtp");
        api = new Privat24Api("db201de4-90be-4003-b210-010e56f96c83", linkData.login, linkData.password);
    }

    /**
     * Should be run first. Then copy the id from logs and run testAuthenticateWithOtp
     */
    public void testAuthenticateWithPhoneAndPass() throws IOException, PrivatBankException {
        String id = api.authenticateWithPhoneAndPass();
        assertNotNull(id, "The id was null");
        LogUtil.d(this, "Authenticated by phone and pass. Operation Id: " + id + ". Should be used to authenticate with OTP");
    }

    /**
     * Should be run after the testAuthenticateWithPhoneAndPass with the id copied from logs and the OTP from SMS
     */
    public void testAuthenticateWithOtp() throws IOException, PrivatBankException {
        String cookie = api.authenticateWithOtp("conv_380677132298_18938871063", "9848");
        assertNotNull(cookie, "The cookie was null");
        LogUtil.d(this, "Authentication with the OTP successful. The cookie: " + cookie);
    }
}
