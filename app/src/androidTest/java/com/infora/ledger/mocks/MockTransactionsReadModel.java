package com.infora.ledger.mocks;

import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.data.TransactionsReadModel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jenya on 09.06.15.
 */
public class MockTransactionsReadModel extends TransactionsReadModel {

    private final ArrayList<PendingTransaction> transactions;

    public MockTransactionsReadModel() {
        super(null);
        transactions = new ArrayList<>();
    }

    public PendingTransaction inject(PendingTransaction transaction) {
        transactions.add(transaction);
        return transaction;
    }

    public MockTransactionsReadModel injectAnd(PendingTransaction transaction) {
        inject(transaction);
        return this;
    }

    @Override
    public List<PendingTransaction> getTransactions() throws SQLException {
        return transactions;
    }

    @Override
    public boolean isTransactionExists(String transactionId) throws SQLException {
        for (PendingTransaction transaction : transactions) {
            if(transaction.transactionId == transactionId) return true;
        }
        return false;
    }
}
