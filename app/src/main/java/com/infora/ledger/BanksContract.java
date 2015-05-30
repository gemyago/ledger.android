package com.infora.ledger;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by jenya on 30.05.15.
 */
public class BanksContract {
    public static final String AUTHORITY = "com.infora.ledger.banks-provider";

    public static class BankLinks implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/banks/bank-links");
        public static final String TABLE_NAME = "banks_bank_links";

        public static final String COLUMN_BIC = "bank_identifier_code";
        public static final String COLUMN_ACCOUNT_ID = "account_id";
        public static final String COLUMN_LINK_DATA = "link_data";
    }
}