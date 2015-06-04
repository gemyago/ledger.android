package com.infora.ledger.data;

import android.database.Cursor;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import static com.infora.ledger.TransactionContract.COLUMN_ACCOUNT_ID;
import static com.infora.ledger.TransactionContract.COLUMN_AMOUNT;
import static com.infora.ledger.TransactionContract.COLUMN_BIC;
import static com.infora.ledger.TransactionContract.COLUMN_COMMENT;
import static com.infora.ledger.TransactionContract.COLUMN_ID;
import static com.infora.ledger.TransactionContract.COLUMN_IS_DELETED;
import static com.infora.ledger.TransactionContract.COLUMN_IS_PUBLISHED;
import static com.infora.ledger.TransactionContract.COLUMN_TIMESTAMP;
import static com.infora.ledger.TransactionContract.COLUMN_TRANSACTION_ID;
import static com.infora.ledger.TransactionContract.TABLE_NAME;


/**
 * Created by jenya on 01.03.15.
 */
@DatabaseTable(tableName = TABLE_NAME)
public class PendingTransaction {
    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    public int id;

    @DatabaseField(columnName = COLUMN_ACCOUNT_ID)
    public String accountId;

    @DatabaseField(columnName = COLUMN_TRANSACTION_ID)
    public String transactionId;

    @DatabaseField(columnName = COLUMN_AMOUNT)
    public String amount;

    @DatabaseField(columnName = COLUMN_COMMENT)
    public String comment;

    @DatabaseField(columnName = COLUMN_IS_PUBLISHED)
    public boolean isPublished;

    @DatabaseField(columnName = COLUMN_IS_DELETED)
    public boolean isDeleted;

    @DatabaseField(columnName = COLUMN_TIMESTAMP)
    public Date timestamp;

    @DatabaseField(columnName = COLUMN_BIC)
    public String bic;

    public PendingTransaction() {
    }

    public PendingTransaction(Cursor cursor) {
        id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        transactionId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID));
        amount = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT));
        comment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT));
        isPublished = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_PUBLISHED)) == 1;
        isDeleted = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_DELETED)) == 1;
        timestamp = LedgerDbHelper.parseISO8601(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
        bic = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIC));
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

    public PendingTransaction setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public PendingTransaction setTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public PendingTransaction setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public PendingTransaction setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public PendingTransaction setIsPublished(boolean isPublished) {
        this.isPublished = isPublished;
        return this;
    }

    public PendingTransaction setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
        return this;
    }

    public PendingTransaction setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public PendingTransaction setBic(String bic) {
        this.bic = bic;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PendingTransaction that = (PendingTransaction) o;

        if (id != that.id) return false;
        if (isPublished != that.isPublished) return false;
        if (isDeleted != that.isDeleted) return false;
        if (accountId != null ? !accountId.equals(that.accountId) : that.accountId != null)
            return false;
        if (!transactionId.equals(that.transactionId)) return false;
        if (!amount.equals(that.amount)) return false;
        if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
        if (!LedgerDbHelper.toISO8601(timestamp).equals(LedgerDbHelper.toISO8601(that.timestamp)))
            return false;
        return !(bic != null ? !bic.equals(that.bic) : that.bic != null);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (accountId != null ? accountId.hashCode() : 0);
        result = 31 * result + transactionId.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (isPublished ? 1 : 0);
        result = 31 * result + (isDeleted ? 1 : 0);
        result = 31 * result + LedgerDbHelper.toISO8601(timestamp).hashCode();
        result = 31 * result + (bic != null ? bic.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PendingTransaction{" +
                "id=" + id +
                ", accountId='" + accountId + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", amount='" + amount + '\'' +
                ", comment='" + comment + '\'' +
                ", isPublished=" + isPublished +
                ", isDeleted=" + isDeleted +
                ", timestamp='" + LedgerDbHelper.toISO8601(timestamp) + '\'' +
                ", bic='" + bic + '\'' +
                '}';
    }
}
