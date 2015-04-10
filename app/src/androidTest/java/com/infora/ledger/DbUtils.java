package com.infora.ledger;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.infora.ledger.data.LedgerDbHelper;

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

    public static int insertPendingTransaction(SQLiteOpenHelper dbHelper, String transactionId, String amount, String comment) {
        return insertPendingTransaction(dbHelper, transactionId, amount, comment, false);
    }

    public static int insertPendingTransaction(SQLiteOpenHelper dbHelper, String transactionId, String amount, String comment, boolean isPublished) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(TransactionContract.COLUMN_TRANSACTION_ID, transactionId);
            values.put(TransactionContract.COLUMN_AMOUNT, amount);
            values.put(TransactionContract.COLUMN_COMMENT, comment);
            values.put(TransactionContract.COLUMN_IS_PUBLISHED, isPublished);
            values.put(TransactionContract.COLUMN_TIMESTAMP, LedgerDbHelper.toISO8601(new Date()));
            return (int) db.insertOrThrow(TransactionContract.TABLE_NAME, null, values);
        } finally {
            db.close();
        }
    }
}
