package com.infora.ledger.banks;

import android.test.AndroidTestCase;
import android.util.Log;

import com.infora.ledger.support.LogUtil;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by jenya on 23.05.15.
 */
public class PrivatBankApiManualTest extends AndroidTestCase {

    private PrivatBankApi api;

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
        api = new PrivatBankApi();
    }

    public void testGetTransactions() throws IOException {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.DATE, -1);
        Date yesterday = calendar.getTime();

        GetTransactionsRequest request = new GetTransactionsRequest(
                "TODO (do not commit)",
                "TODO (do not commit)",
                "TODO (do not commit)",
                yesterday,
                now);
        List<PrivatBankTransaction> transactions = api.getTransactions(request);
        LogUtil.d(this, "Fetched transactions " + transactions.size());
        for (PrivatBankTransaction transaction : transactions) {
            LogUtil.d(this, transaction.toString());
        }
    }
}
