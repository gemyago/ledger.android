package com.infora.ledger;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by jenya on 01.03.15.
 */
public class PendingTransactionContract implements BaseColumns {
    public static final String AUTHORITY = "com.infora.ledger.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/pending-transactions");

    public static final String TABLE_NAME = "pending_transactions";

    public static final String COLUMN_ID = _ID;
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_COMMENT= "content";
    public static final String[] ALL_COLUMNS = new String[]{
            PendingTransactionContract.COLUMN_ID,
            PendingTransactionContract.COLUMN_AMOUNT,
            PendingTransactionContract.COLUMN_COMMENT
    };
}
