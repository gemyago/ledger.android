package com.infora.ledger;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by jenya on 01.03.15.
 */
public class TransactionContract implements BaseColumns {
    public static final String AUTHORITY = "com.infora.ledger.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/pending-transactions");

    public static final String TABLE_NAME = "pending_transactions";

    public static final String COLUMN_ID = _ID;
    public static final String COLUMN_TRANSACTION_ID = "transaction_id";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_COMMENT= "content";
    public static final String COLUMN_IS_PUBLISHED= "is_published";
    public static final String COLUMN_IS_DELETED= "is_deleted";
    public static final String COLUMN_TIMESTAMP= "timestamp";

    public static final String[] ALL_COLUMNS = new String[]{
            TransactionContract.COLUMN_ID,
            TransactionContract.COLUMN_TRANSACTION_ID,
            TransactionContract.COLUMN_AMOUNT,
            TransactionContract.COLUMN_COMMENT,
            TransactionContract.COLUMN_IS_PUBLISHED,
            TransactionContract.COLUMN_IS_DELETED,
            TransactionContract.COLUMN_TIMESTAMP
    };

    public static final String[] ASSIGNABLE_COLUMNS = new String[]{
            TransactionContract.COLUMN_TRANSACTION_ID,
            TransactionContract.COLUMN_AMOUNT,
            TransactionContract.COLUMN_COMMENT,
            TransactionContract.COLUMN_IS_PUBLISHED,
            TransactionContract.COLUMN_IS_DELETED,
            TransactionContract.COLUMN_TIMESTAMP
    };
}
