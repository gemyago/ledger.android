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
     * Gets transactions fetched from bank for specified bic and dates range
     *
     * @param bic  - bic to get transactions for
     * @param from - date from inclusive. Note: time portion is ignored.
     * @param to   - date to inclusive. Note: time portion is ignored.
     * @return
     */
    public List<PendingTransaction> getTransactionsFetchedFromBank(String bic, Date from, Date to) throws SQLException {
        ConnectionSource connectionSource = new AndroidConnectionSource(dbHelper);
        try {
            Dao<PendingTransaction, Long> dao = DaoManager.createDao(connectionSource, PendingTransaction.class);
            QueryBuilder<PendingTransaction, Long> builder = dao.queryBuilder();
            builder.where().between(TransactionContract.COLUMN_TIMESTAMP, Dates.startOfDay(from), Dates.endOfDay(to));
            return builder.query();
        } finally {
            connectionSource.close();
        }

    }
}
