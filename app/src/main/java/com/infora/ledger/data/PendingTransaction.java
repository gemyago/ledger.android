package com.infora.ledger.data;

import android.database.Cursor;

import com.infora.ledger.TransactionContract;

import java.util.Date;

/**
 * Created by jenya on 01.03.15.
 */
public class PendingTransaction {
    public int id;
    public String accountId;
    public String transactionId;
    public String amount;
    public String comment;
    public boolean isPublished;
    public boolean isDeleted;
    public Date timestamp;
    public String bic;

    public PendingTransaction(Cursor cursor) {
        id = cursor.getInt(cursor.getColumnIndexOrThrow(TransactionContract.COLUMN_ID));
        transactionId = cursor.getString(cursor.getColumnIndexOrThrow(TransactionContract.COLUMN_TRANSACTION_ID));
        amount = cursor.getString(cursor.getColumnIndexOrThrow(TransactionContract.COLUMN_AMOUNT));
        comment = cursor.getString(cursor.getColumnIndexOrThrow(TransactionContract.COLUMN_COMMENT));
        isPublished = cursor.getInt(cursor.getColumnIndexOrThrow(TransactionContract.COLUMN_IS_PUBLISHED)) == 1;
        isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow(TransactionContract.COLUMN_IS_DELETED)) == 1;
        timestamp = LedgerDbHelper.parseISO8601(cursor.getString(cursor.getColumnIndexOrThrow(TransactionContract.COLUMN_TIMESTAMP)));
        bic = cursor.getString(cursor.getColumnIndexOrThrow(TransactionContract.COLUMN_BIC));
    }

    public PendingTransaction(int id, String amount, String comment) {
        this.id = id;
        this.amount = amount;
        this.comment = comment;
    }

    public PendingTransaction(String transactionId, String amount, String comment, boolean isPublished, boolean isDeleted, Date timestamp, String bic) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.comment = comment;
        this.isPublished = isPublished;
        this.isDeleted = isDeleted;
        this.timestamp = timestamp;
        this.bic = bic;
    }

    public PendingTransaction setId(int id) {
        this.id = id;
        return this;
    }
}
