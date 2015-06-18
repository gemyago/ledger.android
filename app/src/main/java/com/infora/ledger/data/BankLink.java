package com.infora.ledger.data;

import android.database.Cursor;

import com.google.gson.Gson;
import com.infora.ledger.support.Dates;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import static com.infora.ledger.BanksContract.BankLinks.COLUMN_ACCOUNT_ID;
import static com.infora.ledger.BanksContract.BankLinks.COLUMN_ACCOUNT_NAME;
import static com.infora.ledger.BanksContract.BankLinks.COLUMN_BIC;
import static com.infora.ledger.BanksContract.BankLinks.COLUMN_HAS_SUCCEED;
import static com.infora.ledger.BanksContract.BankLinks.COLUMN_IN_PROGRESS;
import static com.infora.ledger.BanksContract.BankLinks.COLUMN_LAST_SYNC_DATE;
import static com.infora.ledger.BanksContract.BankLinks.COLUMN_LINK_DATA;
import static com.infora.ledger.BanksContract.BankLinks.TABLE_NAME;
import static com.infora.ledger.BanksContract.BankLinks._ID;

/**
 * Created by jenya on 30.05.15.
 */
@DatabaseTable(tableName = TABLE_NAME)
public class BankLink implements Entity {
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

    @DatabaseField(columnName = COLUMN_LAST_SYNC_DATE)
    public Date lastSyncDate;

    @DatabaseField(columnName = COLUMN_IN_PROGRESS)
    public boolean isInProgress;

    @DatabaseField(columnName = COLUMN_HAS_SUCCEED)
    public boolean hasSucceed;

    @Override
    public int getId() {
        return id;
    }

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

    public BankLink setLastSyncDate(Date value) {
        lastSyncDate = value;
        return this;
    }

    public BankLink setInProgress(Boolean value) {
        isInProgress = value;
        return this;
    }

    public BankLink setHasSucceed(Boolean value) {
        hasSucceed = value;
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
        lastSyncDate = LedgerDbHelper.parseISO8601(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_SYNC_DATE)));
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
        if (isInProgress != bankLink.isInProgress) return false;
        if (hasSucceed != bankLink.hasSucceed) return false;
        if (!accountId.equals(bankLink.accountId)) return false;
        if (!accountName.equals(bankLink.accountName)) return false;
        if (!bic.equals(bankLink.bic)) return false;
        if (!linkData.equals(bankLink.linkData)) return false;
        return Dates.areEqual(lastSyncDate, bankLink.lastSyncDate);

    }


    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + accountId.hashCode();
        result = 31 * result + accountName.hashCode();
        result = 31 * result + bic.hashCode();
        result = 31 * result + linkData.hashCode();
        result = 31 * result + (lastSyncDate != null ? LedgerDbHelper.toISO8601(lastSyncDate).hashCode() : 0);
        result = 31 * result + (isInProgress ? 1 : 0);
        result = 31 * result + (hasSucceed ? 1 : 0);
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
                ", lastSyncDate='" + (lastSyncDate == null ? null : LedgerDbHelper.toISO8601(lastSyncDate)) + '\'' +
                ", isInProgress=" + isInProgress +
                ", hasSucceed=" + hasSucceed +
                '}';
    }
}
