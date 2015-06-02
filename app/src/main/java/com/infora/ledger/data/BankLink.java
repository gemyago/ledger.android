package com.infora.ledger.data;

import android.database.Cursor;

import com.google.gson.Gson;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import static com.infora.ledger.BanksContract.BankLinks.COLUMN_ACCOUNT_ID;
import static com.infora.ledger.BanksContract.BankLinks.COLUMN_ACCOUNT_NAME;
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

    @DatabaseField(columnName = COLUMN_ACCOUNT_ID)
    public String accountId;

    @DatabaseField(columnName = COLUMN_ACCOUNT_NAME)
    public String accountName;

    @DatabaseField(columnName = COLUMN_BIC)
    public String bic;

    @DatabaseField(columnName = COLUMN_LINK_DATA)
    public String linkData;

    public BankLink setId(int id) {
        this.id = id;
        return this;
    }

    public BankLink setAccountId(String value) {
        accountId = value;
        return this;
    }

    public BankLink setAccountName(String value) {
        accountName = value;
        return this;
    }

    public BankLink setBic(String value) {
        bic = value;
        return this;
    }

    public BankLink setLinkDataValue(String value) {
        linkData = value;
        return this;
    }

    public BankLink() {
    }

    public BankLink(Cursor cursor) {
        id = cursor.getInt(cursor.getColumnIndexOrThrow(_ID));
        accountId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACCOUNT_ID));
        accountName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACCOUNT_NAME));
        bic = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIC));
        linkData = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LINK_DATA));
    }

    public <T> T getLinkData(Class<T> classOfT) {
        return new Gson().fromJson(linkData, classOfT);
    }

    public <T> BankLink setLinkData(T data) {
        linkData = new Gson().toJson(data);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BankLink bankLink = (BankLink) o;

        if (id != bankLink.id) return false;
        if (!accountId.equals(bankLink.accountId)) return false;
        if (!accountName.equals(bankLink.accountName)) return false;
        if (!bic.equals(bankLink.bic)) return false;
        return linkData.equals(bankLink.linkData);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + accountId.hashCode();
        result = 31 * result + accountName.hashCode();
        result = 31 * result + bic.hashCode();
        result = 31 * result + linkData.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BankLink{" +
                "id=" + id +
                ", accountId='" + accountId + '\'' +
                ", accountName='" + accountName + '\'' +
                ", bic='" + bic + '\'' +
                ", linkData='" + linkData + '\'' +
                '}';
    }
}
