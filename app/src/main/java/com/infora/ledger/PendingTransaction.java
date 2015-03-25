package com.infora.ledger;

import android.content.ContentValues;
import android.database.Cursor;

import com.infora.ledger.data.LedgerDbHelper;

import java.util.Date;

/**
 * Created by jenya on 01.03.15.
 */
public class PendingTransaction {
    private int id;
    private String transactionId;
    private String amount;
    private String comment;
    private Boolean isPublished;
    private Date timestamp;

    public PendingTransaction(Cursor cursor) {
        setId(getId(cursor));
        setTransactionId(cursor.getString(cursor.getColumnIndexOrThrow(PendingTransactionContract.COLUMN_TRANSACTION_ID)));
        setAmount(cursor.getString(cursor.getColumnIndexOrThrow(PendingTransactionContract.COLUMN_AMOUNT)));
        setComment(cursor.getString(cursor.getColumnIndexOrThrow(PendingTransactionContract.COLUMN_COMMENT)));
        setIsPublished(cursor.getInt(cursor.getColumnIndexOrThrow(PendingTransactionContract.COLUMN_IS_PUBLISHED)) == 1);
        String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(PendingTransactionContract.COLUMN_TIMESTAMP));
        setTimestamp(LedgerDbHelper.parseISO8601(timestamp));
    }

    public static ContentValues appendValues(ContentValues values, String amount, String comment) {
        values.put(PendingTransactionContract.COLUMN_AMOUNT, amount);
        values.put(PendingTransactionContract.COLUMN_COMMENT, comment);
        return values;
    }

    public static int getId(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(PendingTransactionContract.COLUMN_ID));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean isPublished() {
        return isPublished;
    }

    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
