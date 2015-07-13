package com.infora.ledger.banks.ua.privatbank;

import android.test.AndroidTestCase;

import com.infora.ledger.banks.BankApi;
import com.infora.ledger.banks.BankTransaction;
import com.infora.ledger.banks.FetchException;
import com.infora.ledger.banks.GetTransactionsRequest;
import com.infora.ledger.banks.ua.privatbank.PrivatBankApi;
import com.infora.ledger.banks.ua.privatbank.PrivatBankException;
import com.infora.ledger.banks.ua.privatbank.PrivatBankLinkData;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.support.LogUtil;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by jenya on 23.05.15.
 */
public class PrivatBankApiManualTest extends AndroidTestCase {

    private BankApi api;
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
        api = new PrivatBankApi();
        final PrivatBankLinkData linkData = new PrivatBankLinkData("TODO (do not commit)",
                "TODO (do not commit)",
                "TODO (do not commit)");
        bankLink = new BankLink().setLinkData(linkData);
    }

    public void testGetTransactions() throws IOException, FetchException {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.DATE, -5);
        Date yesterday = calendar.getTime();

        GetTransactionsRequest request = new GetTransactionsRequest(
                bankLink,
                yesterday,
                now);

        List<BankTransaction> transactions = api.getTransactions(request);
        LogUtil.d(this, "Fetched transactions " + transactions.size());
        for (BankTransaction transaction : transactions) {
            LogUtil.d(this, transaction.toString());
        }
    }
}
