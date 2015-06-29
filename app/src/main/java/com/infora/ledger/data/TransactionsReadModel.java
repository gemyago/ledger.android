package com.infora.ledger.data;

import android.content.Context;
import android.text.format.DateUtils;

import com.infora.ledger.TransactionContract;
import com.infora.ledger.support.Dates;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by jenya on 09.06.15.
 */
public class TransactionsReadModel {

    private final LedgerDbHelper dbHelper;

    public TransactionsReadModel(Context context) {
        dbHelper = new LedgerDbHelper(context);
    }

    /**
     * Returns actual (not deleted) transactions
     *
     * @return
     */
    public List<PendingTransaction> getTransactions() throws SQLException {
        ConnectionSource connectionSource = new AndroidConnectionSource(dbHelper);
        try {
            Dao<PendingTransaction, Long> dao = DaoManager.createDao(connectionSource, PendingTransaction.class);
            QueryBuilder<PendingTransaction, Long> builder = dao.queryBuilder();
            builder.where().eq(TransactionContract.COLUMN_IS_DELETED, false);
            builder.orderBy(TransactionContract.COLUMN_TIMESTAMP, false);
            return builder.query();
        } finally {
            connectionSource.close();
        }
    }

    public boolean isTransactionExists(String transactionId) throws SQLException {
        ConnectionSource connectionSource = new AndroidConnectionSource(dbHelper);
        try {
            Dao<PendingTransaction, Long> dao = DaoManager.createDao(connectionSource, PendingTransaction.class);
            QueryBuilder<PendingTransaction, Long> builder = dao.queryBuilder();
            return builder.where().eq(TransactionContract.COLUMN_TRANSACTION_ID, transactionId).countOf() > 0;
        } finally {
            connectionSource.close();
        }
    }
}
