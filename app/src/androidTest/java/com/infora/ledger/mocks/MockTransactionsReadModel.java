package com.infora.ledger.mocks;

import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.data.TransactionsReadModel;

import junit.framework.ComparisonFailure;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jenya on 09.06.15.
 */
public class MockTransactionsReadModel extends TransactionsReadModel {

    public ArrayList<PendingTransaction> transactionsFetchedFromBank = new ArrayList<>();
    public GetTransactionsFetchedFromBankParams expectedTransactionsFetchedFromBankParams;

    @Override
    public List<PendingTransaction> getTransactionsFetchedFromBank(String bic, Date from, Date to) {
        GetTransactionsFetchedFromBankParams actual = new GetTransactionsFetchedFromBankParams(bic, from, to);
        if (expectedTransactionsFetchedFromBankParams == null) {
            throw new AssertionError("Params expectation not assigned");
        }
        if (!actual.equals(expectedTransactionsFetchedFromBankParams)) {
            throw new ComparisonFailure("Wrong params", expectedTransactionsFetchedFromBankParams.toString(), actual.toString());
        }
        return transactionsFetchedFromBank;
    }
}
