package com.infora.ledger;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jenya on 01.03.15.
 */
public class PendingTransactionsDbUtils {
    private static PendingTransaction getSingleTransaction(Cursor result) {
        PendingTransaction pendingTransaction;
        if (result.getCount() > 1)
            throw new ObjectNotFoundException("Several PendingTransactions(" + result.getCount() + ") were found which is very wrong.");
        result.moveToFirst();
        pendingTransaction = new PendingTransaction(result);
        return pendingTransaction;
    }

    public static PendingTransaction getById(SQLiteOpenHelper dbHelper, long id) {
        return getById(dbHelper, (int) id);
    }

    public static PendingTransaction getById(SQLiteOpenHelper dbHelper, int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        PendingTransaction pendingTransaction;
        try {
            Cursor result = db.query(PendingTransactionContract.TABLE_NAME, null,
                    PendingTransactionContract.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null, null, null);
            if (result.getCount() == 0)
                throw new ObjectNotFoundException("PendingTransaction with id='" + id + "' was not found.");
            pendingTransaction = getSingleTransaction(result);
            result.close();
        } finally {
            db.close();
        }
        return pendingTransaction;
    }

    public static PendingTransaction getByTransactionId(SQLiteOpenHelper dbHelper, String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        PendingTransaction pendingTransaction;
        try {
            Cursor result = db.query(PendingTransactionContract.TABLE_NAME, null,
                    PendingTransactionContract.COLUMN_TRANSACTION_ID + " = ?",
                    new String[]{id},
                    null, null, null);
            if (result.getCount() == 0)
                throw new ObjectNotFoundException("PendingTransaction with transaction_id='" + id + "' was not found.");
            pendingTransaction = getSingleTransaction(result);
            result.close();
        } finally {
            db.close();
        }
        return pendingTransaction;
    }
}
