package com.infora.ledger.mocks;

import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.data.TransactionsReadModel;
import com.infora.ledger.support.ObjectNotFoundException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Override public PendingTransaction getById(int id) {
        for (PendingTransaction transaction : transactions) {
            if(transaction.id == id) return transaction;
        }
        throw new ObjectNotFoundException("Transaction id='" + id + "' not found");
    }

    @Override
    public List<PendingTransaction> getTransactions() throws SQLException {
        final ArrayList<PendingTransaction> result = new ArrayList<>();
        for(PendingTransaction transaction : transactions) {
            if(!transaction.isDeleted) result.add(transaction);
        }
        return result;
    }

    @Override
    public boolean isTransactionExists(String transactionId) throws SQLException {
        for (PendingTransaction transaction : transactions) {
            if(Objects.equals(transaction.transactionId, transactionId)) return true;
        }
        return false;
    }
}
