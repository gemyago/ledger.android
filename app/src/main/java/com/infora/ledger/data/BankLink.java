package com.infora.ledger.data;

import android.database.Cursor;
import android.util.Base64;

import com.google.gson.Gson;
import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.support.Dates;
import com.infora.ledger.support.ObfuscatedString;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

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

    public <T> T getLinkData(Class<T> classOfT, DeviceSecret secret) {
        byte[] rawLinkDataBytes = Base64.decode(linkData, Base64.DEFAULT);

        //First 16 bytes (128 bits) is the IV
        byte[] iv = new byte[16];
        System.arraycopy(rawLinkDataBytes, 0, iv, 0, 16);

        byte[] actualLinkDataBytes = new byte[rawLinkDataBytes.length - 16];
        System.arraycopy(rawLinkDataBytes, 16, actualLinkDataBytes, 0, actualLinkDataBytes.length);


        try {
            Cipher cipher = Cipher.getInstance(DeviceSecret.ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secret.keySpec(), new IvParameterSpec(iv));
            return new Gson().fromJson(new String(cipher.doFinal(actualLinkDataBytes)), classOfT);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt link data.", e);
        }
    }

    public <T> BankLink setLinkData(T data, DeviceSecret secret) {
        try {
            IvParameterSpec iv = generateIv();
            Cipher cipher = Cipher.getInstance(DeviceSecret.ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secret.keySpec(), iv);

            String jsonData = new Gson().toJson(data);
            byte[] encryptedData = cipher.doFinal(jsonData.getBytes());

            //Joining iv and encrypted data
            byte[] finalData = new byte[iv.getIV().length + encryptedData.length];
            System.arraycopy(iv.getIV(), 0, finalData, 0, iv.getIV().length);
            System.arraycopy(encryptedData, 0, finalData, iv.getIV().length, encryptedData.length);

            linkData = Base64.encodeToString(finalData, Base64.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt link data", e);
        }
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
        //Link data is no longer part of equality since it is encrypted and will be different for the same value
        //if (!linkData.equals(bankLink.linkData)) return false;
        return Dates.areEqual(lastSyncDate, bankLink.lastSyncDate);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + accountId.hashCode();
        result = 31 * result + accountName.hashCode();
        result = 31 * result + bic.hashCode();
        //Link data is no longer part of equality since it is encrypted and will be different for the same value
        //result = 31 * result + linkData.hashCode();
        result = 31 * result + (lastSyncDate != null ? LedgerDbHelper.toISO8601(lastSyncDate).hashCode() : 0);
        result = 31 * result + (isInProgress ? 1 : 0);
        result = 31 * result + (hasSucceed ? 1 : 0);
        return result;
    }


    @Override
    public String toString() {
        return "BankLink{" +
                "id=" + id +
                ", accountId='" + ObfuscatedString.value(accountId) + '\'' +
                ", accountName='" + ObfuscatedString.value(accountName) + '\'' +
                ", bic='" + bic + '\'' +
                ", linkData='" + linkData + '\'' +
                ", lastSyncDate='" + (lastSyncDate == null ? null : LedgerDbHelper.toISO8601(lastSyncDate)) + '\'' +
                ", isInProgress=" + isInProgress +
                ", hasSucceed=" + hasSucceed +
                '}';
    }

    public static IvParameterSpec generateIv() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(DeviceSecret.KEY_ALGORITHM);
        keyGen.init(128);
        SecretKey secretKey = keyGen.generateKey();
        return new IvParameterSpec(secretKey.getEncoded());
    }
}
