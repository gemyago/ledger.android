package com.infora.ledger.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.infora.ledger.BanksContract;
import com.infora.ledger.DbUtils;
import com.infora.ledger.PendingTransaction;
import com.infora.ledger.TransactionContract;

import java.sql.SQLException;

/**
 * Created by jenya on 30.05.15.
 */
public class BanksContentProviderTest extends ProviderTestCase2<BanksContentProvider> {

    private MockContentResolver resolver;
    private LedgerDbHelper dbHelper;

    public BanksContentProviderTest() {
        super(BanksContentProvider.class, BanksContentProvider.AUTHORITY);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        resolver = getMockContentResolver();
        dbHelper = new LedgerDbHelper(getMockContext());
    }

    public void testGetType() throws Exception {
        assertEquals(BanksContentProvider.BANK_LINKS_LIST_TYPE, resolver.getType(BanksContract.BankLinks.CONTENT_URI));

        Uri itemUrl = Uri.parse("content://" + BanksContract.AUTHORITY + "/banks/bank-links/100");
        assertEquals(BanksContentProvider.BANK_LINKS_ITEM_TYPE, resolver.getType(itemUrl));
    }

    public void testInsertNewBankLink() throws SQLException {
        ContentValues values = new ContentValues();
        values.put(BanksContract.BankLinks.COLUMN_ACCOUNT_ID, "account-100");
        values.put(BanksContract.BankLinks.COLUMN_BIC, "BANK-100");
        values.put(BanksContract.BankLinks.COLUMN_LINK_DATA, "account-data-100");
        Uri newUri = resolver.insert(BanksContract.BankLinks.CONTENT_URI, values);
        long id = ContentUris.parseId(newUri);
        assertFalse(id == 0);
        assertEquals(BanksContract.BankLinks.CONTENT_URI + "/" + id, newUri.toString());

        BankLink bankLink = DbUtils.getBankLinkById(dbHelper, id);
        assertEquals("account-100", bankLink.account_id);
        assertEquals("BANK-100", bankLink.bic);
        assertEquals("account-data-100", bankLink.link_data);
    }

}