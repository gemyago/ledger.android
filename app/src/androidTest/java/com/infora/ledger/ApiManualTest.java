package com.infora.ledger;

import android.test.AndroidTestCase;

import com.infora.ledger.api.ApiAdapter;
import com.infora.ledger.api.AuthenticityToken;
import com.infora.ledger.api.LedgerApi;

import java.util.Date;
import java.util.UUID;

/**
 * Created by jenya on 12.03.15.
 */
public class ApiManualTest extends AndroidTestCase {
    /**
     * Before running tests please specify api endpoint url
     */
    private String endpointUrl = "http://rj45:3000";

    private ApiAdapter adapter;
    private LedgerApi ledgerApi;

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
        adapter = new ApiAdapter(endpointUrl);
        ledgerApi = adapter.getLedgerApi();
    }

    public void testReportPendingTransaction() throws InterruptedException {
        AuthenticityToken authenticityToken = ledgerApi.authenticateByIdToken("dev@domain.com", "password");
        adapter.setAuthenticityToken(authenticityToken.getValue());
        ledgerApi.reportPendingTransaction(UUID.randomUUID().toString(), "100.00", "Comment for transaction 100", new Date());
    }
}
