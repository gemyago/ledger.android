package com.infora.ledger.data;

import android.database.Cursor;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import static com.infora.ledger.BanksContract.BankLinks.COLUMN_ACCOUNT_ID;
import static com.infora.ledger.BanksContract.BankLinks.COLUMN_BIC;
import static com.infora.ledger.BanksContract.BankLinks.COLUMN_LINK_DATA;
import static com.infora.ledger.BanksContract.BankLinks.TABLE_NAME;
import static com.infora.ledger.BanksContract.BankLinks._ID;

/**
 * Created by jenya on 30.05.15.
 */
@DatabaseTable(tableName = TABLE_NAME)
public class BankLink {
    @DatabaseField(id = true, columnName = _ID)
    public int id;
    @DatabaseField
    public String account_id;
    @DatabaseField(columnName = COLUMN_BIC)
    public String bic;
    @DatabaseField
    public String link_data;

    public BankLink() {
    }

    public BankLink(Cursor cursor) {
        id = cursor.getInt(cursor.getColumnIndexOrThrow(_ID));
        account_id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACCOUNT_ID));
        bic = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIC));
        link_data = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LINK_DATA));
    }
}
