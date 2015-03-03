package com.infora.ledger;

import android.provider.BaseColumns;

/**
 * Created by jenya on 01.03.15.
 */
public class PendingTransactionContract implements BaseColumns {
    public static final String TABLE_NAME = "pending_transactions";

    public static final String COLUMN_ID = _ID;
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_COMMENT= "content";
}
