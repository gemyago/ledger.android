package com.infora.ledger.mocks;

import com.infora.ledger.banks.GetTransactionsRequest;
import com.infora.ledger.banks.PrivatBankApi;
import com.infora.ledger.banks.PrivatBankException;
import com.infora.ledger.banks.PrivatBankTransaction;

import junit.framework.ComparisonFailure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jenya on 09.06.15.
 */
public class MockPrivatBankApi extends PrivatBankApi {
    public GetTransactionsRequest expectedGetTransactionsRequest;
    public List<PrivatBankTransaction> privatBankTransactions = new ArrayList<>();

    @Override
    public List<PrivatBankTransaction> getTransactions(GetTransactionsRequest request) throws IOException, PrivatBankException {
        if (expectedGetTransactionsRequest == null) {
            throw new AssertionError("GetTransactionRequest expectation not assigned.");
        }
        if (!expectedGetTransactionsRequest.equals(request)) {
            throw new ComparisonFailure("Wrong request", expectedGetTransactionsRequest.toString(), request.toString());
        }
        return privatBankTransactions;
    }
}
