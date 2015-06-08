package com.infora.ledger.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import com.infora.ledger.BanksContract;
import com.infora.ledger.DbUtils;

import java.sql.SQLException;
import java.util.Date;

/**
 * Created by jenya on 30.05.15.
 */
public class BanksContentProviderTest extends ProviderTestCase2<BanksContentProvider> {

    private MockContentResolver resolver;
    private LedgerDbHelper dbHelper;
    private DatabaseRepository<BankLink> repository;

    public BanksContentProviderTest() {
        super(BanksContentProvider.class, BanksContentProvider.AUTHORITY);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        resolver = getMockContentResolver();
        dbHelper = new LedgerDbHelper(getMockContext());
        repository = new DatabaseContext(getMockContext()).createRepository(BankLink.class);
        DbUtils.deleteAllDatabases(getMockContext());
    }

    public void testGetType() throws Exception {
        assertEquals(BanksContentProvider.BANK_LINKS_LIST_TYPE, resolver.getType(BanksContract.BankLinks.CONTENT_URI));

        Uri itemUrl = Uri.parse("content://" + BanksContract.AUTHORITY + "/banks/bank-links/100");
        assertEquals(BanksContentProvider.BANK_LINKS_ITEM_TYPE, resolver.getType(itemUrl));
    }

    public void testQueryBankLinks() throws SQLException {
        BankLink link1 = new BankLink().setAccountId("a-1").setAccountName("A 1").setBic("BANK-1").setLinkDataValue("link-data-1").setLastSyncDate(new Date());
        BankLink link2 = new BankLink().setAccountId("a-2").setAccountName("A 2").setBic("BANK-2").setLinkDataValue("link-data-2").setLastSyncDate(new Date());
        BankLink link3 = new BankLink().setAccountId("a-3").setAccountName("A 3").setBic("BANK-3").setLinkDataValue("link-data-3").setLastSyncDate(new Date());
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