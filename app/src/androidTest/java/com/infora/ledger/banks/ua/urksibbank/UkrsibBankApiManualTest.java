package com.infora.ledger.banks.ua.urksibbank;

import android.os.Environment;

import com.infora.ledger.banks.BankApi;
import com.infora.ledger.banks.FetchException;
import com.infora.ledger.banks.GetTransactionsRequest;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.support.Dates;
import com.infora.ledger.support.LogUtil;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by mye on 7/8/2015.
 */
public class UkrsibBankApiManualTest extends TestCase {
    private BankApi<UkrsibBankTransaction> api;
    private BankLink bankLink;

    @Override
    protected void runTest() throws Throwable {
        boolean shouldRun = false;
//        shouldRun = true; //Uncomment this line to run tests
        if (shouldRun) {
            super.runTest();
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        api = new UkrsibBankApi();

        bankLink = new BankLink()
                .setLinkData(new UkrsibBankLinkData("TODO", "TODO", "TODO"));
    }

    public void testGetTransactionsWrongLoginPassword() throws IOException, FetchException {
        bankLink = new BankLink()
                .setLinkData(new UkrsibBankLinkData("fake", "fake", "fake"));
        boolean errorRaised = false;
        try {
            api.getTransactions(new GetTransactionsRequest(bankLink, Dates.addDays(new Date(), 10), new Date()));
        } catch (FetchException ex) {
            assertEquals("Authentication failed.", ex.getMessage());
            errorRaised = true;
        }
        assertTrue("Authentication error has not been raised", errorRaised);
    }

    public void testGetTransactionsWithingLastMonth() throws IOException, FetchException {
        List<UkrsibBankTransaction> transactions = api.getTransactions(
                new GetTransactionsRequest(bankLink, Dates.addDays(new Date(), 10), new Date()));
        for (UkrsibBankTransaction transaction : transactions) {
            LogUtil.d(this, transaction.toString());
        }
    }
}