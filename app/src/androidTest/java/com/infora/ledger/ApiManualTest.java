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
        accountManager = new AccountManagerWrapper(getContext());
        adapter = new ApiAdapter(accountManager, endpointUrl);
        ledgerApi = adapter.createApi();
        Account account = accountManager.getApplicationAccounts()[0];
        adapter.authenticateApi(ledgerApi, account);
    }

    public void testReportPendingTransaction() throws InterruptedException, AuthenticatorException, OperationCanceledException, IOException {
        ledgerApi.reportPendingTransaction(UUID.randomUUID().toString(), "100.00", "Comment for transaction 100", new Date());
    }

    public void testGetPendingTransactions() throws InterruptedException, AuthenticatorException, OperationCanceledException, IOException {
        ledgerApi.reportPendingTransaction(UUID.randomUUID().toString(), "100.00", "Comment for transaction 100", new Date());
        ledgerApi.reportPendingTransaction(UUID.randomUUID().toString(), "100.01", "Comment for transaction 101", new Date());
        ArrayList<PendingTransactionDto> pendingTransactions = ledgerApi.getPendingTransactions();
        assertFalse("There should be some pending transactions for testing purposes", pendingTransactions.isEmpty());
        for (PendingTransactionDto pendingTransaction : pendingTransactions) {
            assertNotNull(pendingTransaction.transactionId);
            assertNotNull(pendingTransaction.amount);
            assertNotNull(pendingTransaction.comment);
        }
    }

    public void testAdjustPendingTransaction() {
        ArrayList<PendingTransactionDto> transactions = ledgerApi.getPendingTransactions();
        for (PendingTransactionDto transaction : transactions) {
            float newAmount = Float.parseFloat(transaction.amount) + 1;
            ledgerApi.adjustPendingTransaction(
                    transaction.transactionId,
                    String.valueOf(newAmount),
                    "Comment " + newAmount);
        }
    }
}
