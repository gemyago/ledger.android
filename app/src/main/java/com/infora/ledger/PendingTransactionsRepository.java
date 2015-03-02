package com.infora.ledger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by jenya on 01.03.15.
 */
public class PendingTransactionsRepository {
    private static final String TAG = PendingTransactionsRepository.class.getName();

    private SQLiteOpenHelper dbHelper;

    public PendingTransactionsRepository(Context context) {
        this(new LedgerDbHelper(context));
    }

    public PendingTransactionsRepository(SQLiteOpenHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public PendingTransaction getById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        PendingTransaction pendingTransaction = new PendingTransaction();
        try {
            Cursor result = db.query(PendingTransactionContract.TABLE_NAME, null,
                    PendingTransactionContract.COLUMN_ID + " = ?",
                    new String[]{id},
                    null, null, null);
            if (result.getCount() != 1)
                throw new ObjectNotFoundException("PendingTransaction with id='" + id + "' was not found.");
            result.moveToFirst();
            pendingTransaction.setId(result.getString(result.getColumnIndexOrThrow(PendingTransactionContract.COLUMN_ID)));
            pendingTransaction.setAmount(result.getString(result.getColumnIndexOrThrow(PendingTransactionContract.COLUMN_AMOUNT)));
            pendingTransaction.setComment(result.getString(result.getColumnIndexOrThrow(PendingTransactionContract.COLUMN_COMMENT)));
            result.close();
        } finally {
            db.close();
        }
        return pendingTransaction;
    }

    public void save(PendingTransaction transaction) {
        Log.d(TAG, "Saving transaction id='" + transaction.getId() + "'");
        ContentValues values = new ContentValues();
        values.put(PendingTransactionContract.COLUMN_AMOUNT, transaction.getAmount());
        values.put(PendingTransactionContract.COLUMN_COMMENT, transaction.getComment());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            if (isExists(db, transaction.getId())) {
                Log.d(TAG, "Transaction exists. Updating...");
                db.update(PendingTransactionContract.TABLE_NAME, values,
                        PendingTransactionContract.COLUMN_ID + " = ?",
                        new String[]{transaction.getId()});
            } else {
                Log.d(TAG, "New transaction. Creating...");
                values.put(PendingTransactionContract.COLUMN_ID, transaction.getId());
                db.insert(PendingTransactionContract.TABLE_NAME, null, values);
            }
        } finally {
            db.close();
        }
    }

    private boolean isExists(SQLiteDatabase db, String id) {
        Cursor existsCursor = db.rawQuery("SELECT COUNT(*) " +
                "FROM " + PendingTransactionContract.TABLE_NAME + " " +
                "WHERE " + PendingTransactionContract.COLUMN_ID + " = ?", new String[]{id});
        try {
            existsCursor.moveToFirst();
            return existsCursor.getInt(0) != 0;
        } finally {
            existsCursor.close();
        }
    }
}
