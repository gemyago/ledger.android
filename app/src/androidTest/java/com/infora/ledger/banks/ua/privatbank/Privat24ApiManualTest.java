package com.infora.ledger.banks.ua.privatbank;

import android.test.AndroidTestCase;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.BankTransaction;
import com.infora.ledger.banks.FetchException;
import com.infora.ledger.banks.GetTransactionsRequest;
import com.infora.ledger.banks.ua.privatbank.api.Privat24AuthApi;
import com.infora.ledger.banks.ua.privatbank.api.Privat24BankApi;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.support.Dates;
import com.infora.ledger.support.LogUtil;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by mye on 9/10/2015.
 */
public class Privat24ApiManualTest extends AndroidTestCase {
    private Privat24BankApi bankApi;
    private Privat24BankLinkData linkData;
    private DeviceSecret secret;
    private BankLink bankLink;
    private Privat24AuthApi authApi;

    @Override
    protected void runTest() throws Throwable {
        boolean shouldRun = false;
        //shouldRun = true; //Uncomment this line to run tests
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
                .setCardid("TODO: Set from logs after testGetCards");
        String imei = "db201de4-90be-4003-b210-010e56f96c83";
        authApi = new Privat24AuthApi(imei);
        bankApi = new Privat24BankApi(imei, "TODO: Set cookie after authenticateWithPass");
        secret = DeviceSecret.generateNew();
        bankLink = new BankLink().setLinkData(linkData, secret);
    }

    /**
     * Should be run first. Then copy the id from logs and run testAuthenticateWithOtp
     */
    public void testAuthenticateWithPhoneAndPass() throws IOException, PrivatBankException {
        String id = authApi.authenticateWithPhoneAndPass(linkData.login, linkData.password);
        assertNotNull(id, "The id was null");
        LogUtil.d(this, "Authenticated by phone and pass. Operation Id: " + id + ". Should be used to authenticate with OTP");
    }

    /**
     * Should be run after the testAuthenticateWithPhoneAndPass with the id copied from logs and the OTP from SMS
     */
    public void testAuthenticateWithOtp() throws IOException, PrivatBankException {
        String cookie = authApi.authenticateWithOtp("conv_380677132298_18938871063", "9848");
        assertNotNull(cookie, "The cookie was null");
        LogUtil.d(this, "Authentication with the OTP successful. The cookie: " + cookie);
    }

    /**
     * Should be run after the testAuthenticateWithPhoneAndPass with the id copied from logs and the OTP from SMS
     */
    public void testAuthenticateWithPass() throws IOException, PrivatBankException {
        String cookie = authApi.authenticateWithPass(linkData.password);
        assertNotNull(cookie, "The cookie was null");
        LogUtil.d(this, "Authentication with the password successful. The cookie: " + cookie);
    }

    /**
     * This test must be run strictly after testAuthenticateWithOtp and cookie is assigned.
     */
    public void testGetCards() throws IOException, FetchException {
        List<PrivatBankCard> cards = bankApi.getCards();
        LogUtil.d(this, "Fetched cards " + cards.size());
        for (PrivatBankCard card : cards) {
            LogUtil.d(this, card.toString());
        }
    }

    /**
     * This test must be run strictly after testGetCards and cardid assigned.
     */
    public void testGetTransactions() throws IOException, FetchException {
        Date endDate = new Date();
        Date startDate = Dates.monthAgo(endDate);
        LogUtil.d(this, "Fetching transactions. Start date: " + startDate + ", end date: " + endDate);
        List<Privat24Transaction> transactions = bankApi.getTransactions(new GetTransactionsRequest(bankLink, startDate, endDate), secret);
        LogUtil.d(this, "Fetched transactions " + transactions.size());
        for (BankTransaction transaction : transactions) {
            LogUtil.d(this, transaction.toString());
        }

        startDate = Dates.addDays(endDate, -10);
        LogUtil.d(this, "Fetching transactions. Start date: " + startDate + ", end date: " + endDate);
        transactions = bankApi.getTransactions(new GetTransactionsRequest(bankLink, startDate, endDate), secret);
        LogUtil.d(this, "Fetched transactions " + transactions.size());
        for (BankTransaction transaction : transactions) {
            LogUtil.d(this, transaction.toString());
        }

        endDate = Dates.monthAgo(endDate);
        startDate = Dates.addDays(endDate, -10);
        LogUtil.d(this, "Fetching transactions. Start date: " + startDate + ", end date: " + endDate);
        transactions = bankApi.getTransactions(new GetTransactionsRequest(bankLink, startDate, endDate), secret);
        LogUtil.d(this, "Fetched transactions " + transactions.size());
        for (BankTransaction transaction : transactions) {
            LogUtil.d(this, transaction.toString());
        }
    }
}
