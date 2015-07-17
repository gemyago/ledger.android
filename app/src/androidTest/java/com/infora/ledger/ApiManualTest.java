package com.infora.ledger;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.test.AndroidTestCase;

import com.infora.ledger.api.ApiAdapter;
import com.infora.ledger.api.DeviceSecretDto;
import com.infora.ledger.api.LedgerAccountDto;
import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.api.PendingTransactionDto;
import com.infora.ledger.support.AccountManagerWrapper;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by jenya on 12.03.15.
 */
public class ApiManualTest extends AndroidTestCase {
    /**
     * Before running tests please specify api endpoint url
     */
    private String endpointUrl = "TODO: SPECIFY";

    private ApiAdapter adapter;
    private LedgerApi ledgerApi;
    private AccountManagerWrapper accountManager;
    private Account account;

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
        account = accountManager.getApplicationAccounts()[0];
    }

    public void testReportPendingTransaction() throws InterruptedException, AuthenticatorException, OperationCanceledException, IOException {
        adapter.authenticateApi(ledgerApi, account);
        List<LedgerAccountDto> accounts = ledgerApi.getAccounts();
        LedgerAccountDto accountDto = accounts.get(0);
        String transactionId = UUID.randomUUID().toString();
        Date date = new Date();
        ledgerApi.reportPendingTransaction(transactionId, "100.00", date, "Comment for transaction 100", accountDto.id);
        List<PendingTransactionDto> pendingTransactions = ledgerApi.getPendingTransactions();
        for (PendingTransactionDto pendingTransaction : pendingTransactions) {
            if (pendingTransaction.transactionId == transactionId) {
                assertEquals("100.00", pendingTransaction.amount);
                assertEquals("Comment for transaction 100", pendingTransaction.comment);
                assertEquals(accountDto.id, pendingTransaction.account_id);
                assertEquals(accounts.get(0).id, pendingTransaction.account_id);
                break;
            }
        }
    }

    public void testGetPendingTransactions() throws InterruptedException, AuthenticatorException, OperationCanceledException, IOException {
        adapter.authenticateApi(ledgerApi, account);
        List<LedgerAccountDto> accounts = ledgerApi.getAccounts();
        String t1id = UUID.randomUUID().toString();
        String t2id = UUID.randomUUID().toString();
        ledgerApi.reportPendingTransaction(t1id, "100.00", new Date(), "Comment for transaction 100", accounts.get(0).id);
        ledgerApi.reportPendingTransaction(t2id, "100.01", new Date(), "Comment for transaction 101", accounts.get(1).id);
        List<PendingTransactionDto> pendingTransactions = ledgerApi.getPendingTransactions();
        assertFalse("There should be some pending transactions for testing purposes", pendingTransactions.isEmpty());
        for (PendingTransactionDto pendingTransaction : pendingTransactions) {
            assertNotNull(pendingTransaction.transactionId);
            assertNotNull(pendingTransaction.amount);
            assertNotNull(pendingTransaction.comment);
            if (pendingTransaction.transactionId == t1id) {
                assertEquals(accounts.get(0).id, pendingTransaction.account_id);
            }
            if (pendingTransaction.transactionId == t2id) {
                assertEquals(accounts.get(1).id, pendingTransaction.account_id);
            }
        }
    }
    
    public void testAdjustPendingTransaction() {
        adapter.authenticateApi(ledgerApi, account);
        List<LedgerAccountDto> accounts = ledgerApi.getAccounts();
        List<PendingTransactionDto> transactions = ledgerApi.getPendingTransactions();
        for (PendingTransactionDto transaction : transactions) {
            float newAmount = Float.parseFloat(transaction.amount) + 1;
            ledgerApi.adjustPendingTransaction(
                    transaction.transactionId,
                    String.valueOf(newAmount),
                    "Comment " + newAmount,
                    accounts.get(0).id);
        }
    }

    public void testRejectPendingTransactions() {
        adapter.authenticateApi(ledgerApi, account);
        ledgerApi.reportPendingTransaction(UUID.randomUUID().toString(), "100.00", new Date(), "Comment for transaction 100", null);
        ledgerApi.reportPendingTransaction(UUID.randomUUID().toString(), "100.00", new Date(), "Comment for transaction 100", null);
        ledgerApi.reportPendingTransaction(UUID.randomUUID().toString(), "100.00", new Date(), "Comment for transaction 100", null);

        List<PendingTransactionDto> transactions = ledgerApi.getPendingTransactions();
        for (PendingTransactionDto transaction : transactions) {
            ledgerApi.rejectPendingTransaction(transaction.transactionId);
        }
        assertEquals(0, ledgerApi.getPendingTransactions().size());
    }

    public void testGetDeviceSecret() {
        adapter.authenticateApi(ledgerApi, account);
        DeviceSecretDto deviceSecret = adapter.getDeviceSecret(ledgerApi);
        assertNotNull(deviceSecret.secret);
        assertEquals(deviceSecret.secret, adapter.getDeviceSecret(ledgerApi).secret);
    }
}
