package com.infora.ledger.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.infora.ledger.BanksContract;
import com.infora.ledger.DbUtils;
import com.infora.ledger.mocks.MockLedgerApplication;

import java.sql.SQLException;

/**
 * Created by jenya on 30.05.15.
 */
public class BanksContentProviderTest extends ProviderTestCase2<BanksContentProvider> {

    private MockContentResolver resolver;
    private LedgerDbHelper dbHelper;
    private BankLinksRepository repository;

    public BanksContentProviderTest() {
        super(BanksContentProvider.class, BanksContentProvider.AUTHORITY);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        resolver = getMockContentResolver();
        dbHelper = new LedgerDbHelper(getMockContext());
        repository = new BankLinksRepository(getMockContext());
        DbUtils.deleteAllDatabases(getMockContext());
    }

    public void testGetType() throws Exception {
        assertEquals(BanksContentProvider.BANK_LINKS_LIST_TYPE, resolver.getType(BanksContract.BankLinks.CONTENT_URI));

        Uri itemUrl = Uri.parse("content://" + BanksContract.AUTHORITY + "/banks/bank-links/100");
        assertEquals(BanksContentProvider.BANK_LINKS_ITEM_TYPE, resolver.getType(itemUrl));
    }

    public void testInsertNewBankLink() throws SQLException {
        ContentValues values = new ContentValues();
        values.put(BanksContract.BankLinks.COLUMN_ACCOUNT_ID, "account-100");
        values.put(BanksContract.BankLinks.COLUMN_ACCOUNT_NAME, "Account 100");
        values.put(BanksContract.BankLinks.COLUMN_BIC, "BANK-100");
        values.put(BanksContract.BankLinks.COLUMN_LINK_DATA, "account-data-100");
        Uri newUri = resolver.insert(BanksContract.BankLinks.CONTENT_URI, values);
        long id = ContentUris.parseId(newUri);
        assertFalse(id == 0);
        assertEquals(BanksContract.BankLinks.CONTENT_URI + "/" + id, newUri.toString());

        BankLink bankLink = DbUtils.getBankLinkById(dbHelper, id);
        assertEquals("account-100", bankLink.accountId);
        assertEquals("Account 100", bankLink.accountName);
        assertEquals("BANK-100", bankLink.bic);
        assertEquals("account-data-100", bankLink.linkData);
    }

    public void testQueryBankLinks() throws SQLException {
        BankLink link1 = new BankLink().setAccountId("a-1").setAccountName("A 1").setBic("BANK-1").setLinkDataValue("link-data-1");
        BankLink link2 = new BankLink().setAccountId("a-2").setAccountName("A 2").setBic("BANK-2").setLinkDataValue("link-data-2");
        BankLink link3 = new BankLink().setAccountId("a-3").setAccountName("A 3").setBic("BANK-3").setLinkDataValue("link-data-3");
        repository.save(link1);
        repository.save(link2);
        repository.save(link3);

        Cursor result = resolver.query(BanksContract.BankLinks.CONTENT_URI, null, null, null, null);
        assertEquals(3, result.getCount());
        result.moveToFirst();
        assertEquals(link1, new BankLink(result));
        result.moveToNext();
        assertEquals(link2, new BankLink(result));
        result.moveToNext();
        assertEquals(link3, new BankLink(result));
    }
}