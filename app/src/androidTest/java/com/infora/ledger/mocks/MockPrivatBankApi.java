package com.infora.ledger.mocks;

import com.infora.ledger.banks.BankApi;
import com.infora.ledger.banks.BankTransaction;
import com.infora.ledger.banks.GetTransactionsRequest;
import com.infora.ledger.banks.ua.privatbank.PrivatBankApi;
import com.infora.ledger.banks.ua.privatbank.PrivatBankException;
import com.infora.ledger.banks.ua.privatbank.PrivatBankTransaction;

import junit.framework.ComparisonFailure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jenya on 09.06.15.
 */
public class MockPrivatBankApi implements BankApi {
    public GetTransactionsRequest expectedGetTransactionsRequest;
    public List<BankTransaction> bankTransactions = new ArrayList<>();

    @Override
    public List<BankTransaction> getTransactions(GetTransactionsRequest request) throws IOException, PrivatBankException {
        if (expectedGetTransactionsRequest == null) {
            throw new AssertionError("GetTransactionRequest expectation not assigned.");
        }
        if (!expectedGetTransactionsRequest.equals(request)) {
            throw new ComparisonFailure("Wrong request", expectedGetTransactionsRequest.toString(), request.toString());
        }
        return bankTransactions;
    }
}
