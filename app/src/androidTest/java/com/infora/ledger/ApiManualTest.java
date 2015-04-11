package com.infora.ledger;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.test.AndroidTestCase;

import com.infora.ledger.api.ApiAdapter;
import com.infora.ledger.api.AuthenticityToken;
import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.api.PendingTransactionDto;
import com.infora.ledger.support.AccountManagerWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by jenya on 12.03.15.
 */
public class ApiManualTest extends AndroidTestCase {
    /**
     * Before running tests please specify api endpoint url
     */
    private String endpointUrl = "http://10.1.0.19:3000";

    private ApiAdapter adapter;
    private LedgerApi ledgerApi;
    private AccountManagerWrapper accountManager;

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
        adapter = new ApiAdapter(new AccountManagerWrapper(getContext()), endpointUrl);
        ledgerApi = adapter.getLedgerApi();
        accountManager = new AccountManagerWrapper(getContext());
    }

    public void testReportPendingTransaction() throws InterruptedException, AuthenticatorException, OperationCanceledException, IOException {
        authenticateApi();
        ledgerApi.reportPendingTransaction(UUID.randomUUID().toString(), "100.00", "Comment for transaction 100", new Date());
    }

    public void testGetPendingTransactions() throws InterruptedException, AuthenticatorException, OperationCanceledException, IOException {
        authenticateApi();
        ArrayList<PendingTransactionDto> pendingTransactions = ledgerApi.getPendingTransactions();
        assertFalse("There should be some pending transactions for testing purposes", pendingTransactions.isEmpty());
        for (PendingTransactionDto pendingTransaction : pendingTransactions) {
            assertNotNull(pendingTransaction.transactionId);
            assertNotNull(pendingTransaction.amount);
            assertNotNull(pendingTransaction.comment);
        }
    }

    private void authenticateApi() throws AuthenticatorException, OperationCanceledException, IOException {
        Account account = accountManager.getApplicationAccounts()[0];
        String authToken = accountManager.getAuthToken(account, null);
        AuthenticityToken authenticityToken = ledgerApi.authenticateByIdToken(authToken);
        adapter.setAuthenticityToken(authenticityToken.getValue());
    }
}
