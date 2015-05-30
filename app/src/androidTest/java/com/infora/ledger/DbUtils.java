package com.infora.ledger;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.LedgerDbHelper;
import com.infora.ledger.data.PendingTransaction;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.Date;

/**
 * Created by jenya on 02.03.15.
 */
public class DbUtils {
    public static void deleteAllDatabases(Context context) {
        for (String database : context.databaseList()) {
            context.deleteDatabase(database);
        }
    }

    public static BankLink getBankLinkById(SQLiteOpenHelper dbHelper, long id) throws SQLException {
        ConnectionSource connectionSource = new AndroidConnectionSource(dbHelper);
        try {
            Dao<BankLink,Long> dao = DaoManager.createDao(connectionSource, BankLink.class);
            return dao.queryForId(id);
        } finally {
            connectionSource.close();
        }
    }

    public static Object[] toArray(PendingTransaction transaction) {
        return new Object[]{
                transaction.id,
                transaction.accountId,
                transaction.transactionId,
                transaction.amount,
                transaction.comment,
                transaction.isPublished ? 1 : 0,
                transaction.isDeleted ? 1 : 0,
                LedgerDbHelper.toISO8601(transaction.timestamp == null ? new Date() : transaction.timestamp),
                transaction.bic};
    }

    public static int insertPendingTransaction(SQLiteOpenHelper dbHelper, String transactionId, String amount, String comment) {
        return insertPendingTransaction(dbHelper, transactionId, amount, comment, false, false);
    }

    public static int insertPendingTransaction(SQLiteOpenHelper dbHelper, String transactionId, String amount, String comment, String bic) {
        return insertPendingTransaction(dbHelper, new PendingTransaction(transactionId, amount, comment, false, false, new Date(), bic));
    }

    public static int insertPendingTransaction(SQLiteOpenHelper dbHelper, String transactionId, String amount, String comment, boolean isPublished, boolean isDeleted) {
        PendingTransaction transaction = new PendingTransaction(transactionId, amount, comment, isPublished, isDeleted, new Date(), null);
        return insertPendingTransaction(dbHelper, transaction);
    }

    public static int insertPendingTransaction(SQLiteOpenHelper dbHelper, PendingTransaction transaction) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(TransactionContract.COLUMN_TRANSACTION_ID, transaction.transactionId);
            values.put(TransactionContract.COLUMN_AMOUNT, transaction.amount);
            values.put(TransactionContract.COLUMN_COMMENT, transaction.comment);
            values.put(TransactionContract.COLUMN_IS_PUBLISHED, transaction.isPublished);
            values.put(TransactionContract.COLUMN_IS_DELETED, transaction.isDeleted);
            values.put(TransactionContract.COLUMN_TIMESTAMP, LedgerDbHelper.toISO8601(transaction.timestamp));
            values.put(TransactionContract.COLUMN_BIC, transaction.bic);
            return (int) db.insertOrThrow(TransactionContract.TABLE_NAME, null, values);
        } finally {
            db.close();
        }
    }
}
