package com.infora.ledger;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jenya on 02.03.15.
 */
public class DbUtils {
    public static void deleteAllDatabases(Context context) {
        for (String database : context.databaseList()) {
            context.deleteDatabase(database);
        }
    }

    public static void insertPendingTransaction(SQLiteOpenHelper dbHelper, String id, String amount, String comment) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(PendingTransactionContract.COLUMN_ID, id);
            values.put(PendingTransactionContract.COLUMN_AMOUNT, amount);
            values.put(PendingTransactionContract.COLUMN_COMMENT, comment);
            db.insert(PendingTransactionContract.TABLE_NAME, null, values);
        } finally {
            db.close();
        }
    }
}
