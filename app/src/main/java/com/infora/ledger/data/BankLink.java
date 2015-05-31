package com.infora.ledger.data;

import android.database.Cursor;

import com.google.gson.Gson;
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
    @DatabaseField(columnName = _ID, generatedId = true)
    public int id;
    @DatabaseField
    public String account_id;
    @DatabaseField(columnName = COLUMN_BIC)
    public String bic;
    @DatabaseField
    public String link_data;

    public BankLink setAccountId(String value) {
        account_id = value;
        return this;
    }

    public BankLink setBic(String value) {
        bic = value;
        account_id = value;
        return this;
    }

    public BankLink setLinkDataValue(String value) {
        link_data = value;
        return this;
    }

    public BankLink() {
    }

    public BankLink(Cursor cursor) {
        id = cursor.getInt(cursor.getColumnIndexOrThrow(_ID));
        account_id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACCOUNT_ID));
        bic = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIC));
        link_data = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LINK_DATA));
    }

    public <T> T getLinkData(Class<T> classOfT) {
        return new Gson().fromJson(link_data, classOfT);
    }

    public <T> BankLink setLinkData(T data) {
        link_data = new Gson().toJson(data);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BankLink bankLink = (BankLink) o;

        if (id != bankLink.id) return false;
        if (!account_id.equals(bankLink.account_id)) return false;
        if (!bic.equals(bankLink.bic)) return false;
        return link_data.equals(bankLink.link_data);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + account_id.hashCode();
        result = 31 * result + bic.hashCode();
        result = 31 * result + link_data.hashCode();
        return result;
    }
}
