package com.infora.ledger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jenya on 01.03.15.
 */
public class LedgerDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Ledger";


    public LedgerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + PendingTransactionContract.TABLE_NAME + " (" +
                        PendingTransactionContract.COLUMN_ID + " NVARCHAR(256) PRIMARY KEY NOT NULL," +
                        PendingTransactionContract.COLUMN_AMOUNT + " NVARCHAR(50) NOT NULL," +
                        PendingTransactionContract.COLUMN_COMMENT + " TEXT NULL" +
                        " )"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
