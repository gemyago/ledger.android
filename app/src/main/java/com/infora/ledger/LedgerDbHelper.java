package com.infora.ledger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jenya on 01.03.15.
 */
public class LedgerDbHelper extends SQLiteOpenHelper {
    private static final String TAG = LedgerDbHelper.class.getName();

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Ledger";

    public LedgerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Initializing database schema. Creating table: " + PendingTransactionContract.TABLE_NAME);
        db.execSQL(
                "CREATE TABLE " + PendingTransactionContract.TABLE_NAME + " (" +
                        PendingTransactionContract.COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                        PendingTransactionContract.COLUMN_TRANSACTION_ID + " NVARCHAR(256) NOT NULL UNIQUE," +
                        PendingTransactionContract.COLUMN_AMOUNT + " NVARCHAR(50) NOT NULL," +
                        PendingTransactionContract.COLUMN_COMMENT + " TEXT NULL," +
                        PendingTransactionContract.COLUMN_TIMESTAMP + " NVARCHAR(50) NOT NULL" +
                        " )"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Database upgrade is not yet supported so just recreating the table.");
        //TODO: Implement actual upgrade logic before production
        db.execSQL("DROP TABLE " + PendingTransactionContract.TABLE_NAME);
        onCreate(db);
    }

    private static final SimpleDateFormat ISO8601 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static String toISO8601(Date date) {
        return ISO8601.format(date);
    }
    public static Date parseISO8601(String date) {
        try {
            return ISO8601.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
