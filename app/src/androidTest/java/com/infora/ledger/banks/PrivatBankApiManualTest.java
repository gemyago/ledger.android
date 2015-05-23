package com.infora.ledger.banks;

import android.test.AndroidTestCase;

/**
 * Created by jenya on 23.05.15.
 */
public class PrivatBankApiManualTest extends AndroidTestCase {

    private PrivatBankApi api;

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
    }

    public void testGetTransactions() {
    }
}
