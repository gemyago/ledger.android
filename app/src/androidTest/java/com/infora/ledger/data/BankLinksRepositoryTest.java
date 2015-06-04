package com.infora.ledger.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.infora.ledger.BanksContract.BankLinks;
import com.infora.ledger.DbUtils;
import com.infora.ledger.support.ObjectNotFoundException;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by jenya on 30.05.15.
 */
public class BankLinksRepositoryTest extends AndroidTestCase {
    private LedgerDbHelper dbHelper;
    private BankLinksRepository subject;

    @Override
    public void setUp() throws Exception {
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "-pending-transactions-repo-test");
        DbUtils.deleteAllDatabases(context);
        dbHelper = new LedgerDbHelper(context);
        subject = new BankLinksRepository(context);
    }

    public void testGetById() throws SQLException {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id0, id1;
        Calendar calendarDate = Calendar.getInstance();
        calendarDate.add(Calendar.HOUR, -1);
        Date date0 = calendarDate.getTime();
        calendarDate.add(Calendar.HOUR, -1);
        Date date1 = calendarDate.getTime();
        try {
            ContentValues values = new ContentValues();
            values.put(BankLinks.COLUMN_ACCOUNT_ID, "account-100");
            values.put(BankLinks.COLUMN_ACCOUNT_NAME, "Account 100");
            values.put(BankLinks.COLUMN_BIC, "bank-100");
            values.put(BankLinks.COLUMN_LINK_DATA, "link-data-100");
            values.put(BankLinks.COLUMN_LAST_SYNC_DATE, LedgerDbHelper.toISO8601(date0));
            values.put(BankLinks.COLUMN_IN_PROGRESS, true);
            values.put(BankLinks.COLUMN_HAS_SUCCEED, true);
            id0 = db.insert(BankLinks.TABLE_NAME, null, values);

            values = new ContentValues();
            values.put(BankLinks.COLUMN_ACCOUNT_ID, "account-101");
            values.put(BankLinks.COLUMN_ACCOUNT_NAME, "Account 101");
            values.put(BankLinks.COLUMN_BIC, "bank-101");
            values.put(BankLinks.COLUMN_LINK_DATA, "link-data-101");
            values.put(BankLinks.COLUMN_LAST_SYNC_DATE, LedgerDbHelper.toISO8601(date1));
            values.put(BankLinks.COLUMN_IN_PROGRESS, false);
            values.put(BankLinks.COLUMN_HAS_SUCCEED, false);
            id1 = db.insert(BankLinks.TABLE_NAME, null, values);
        } finally {
            db.close();
        }

        BankLink link0 = subject.getById(id0);
        assertEquals("account-100", link0.accountId);
        assertEquals("Account 100", link0.accountName);
        assertEquals("bank-100", link0.bic);
        assertEquals("link-data-100", link0.linkData);
        assertEquals(date0, link0.lastSyncDate);
        assertEquals(true, link0.isInProgress);
        assertEquals(true, link0.hasSucceed);

        BankLink link1 = subject.getById(id1);
        assertEquals("account-101", link1.accountId);
        assertEquals("Account 101", link1.accountName);
        assertEquals("bank-101", link1.bic);
        assertEquals("link-data-101", link1.linkData);
        assertEquals(date1, link1.lastSyncDate);
        assertEquals(false, link1.isInProgress);
        assertEquals(false, link1.hasSucceed);

        boolean notFoundRaised = false;
        try {
            subject.getById(332234423);
        } catch (ObjectNotFoundException ex) {
            notFoundRaised = true;
        }
        assertTrue("ObjectNotFoundException not raised", notFoundRaised);
    }

    public void testSaveNew() throws SQLException {
        Calendar calendarDate = Calendar.getInstance();
        calendarDate.add(Calendar.HOUR, -1);
        Date date0 = calendarDate.getTime();
        calendarDate.add(Calendar.HOUR, -1);
        Date date1 = calendarDate.getTime();

        BankLink link0 = new BankLink()
                .setAccountId("account-0")
                .setAccountName("Account 0")
                .setBic("bank-0")
                .setLinkDataValue("link-0")
                .setLastSyncDate(date0)
                .setInProgress(true)
                .setHasSucceed(true);

        BankLink link1 = new BankLink()
                .setAccountId("account-1")
                .setAccountName("Account 1")
                .setBic("bank-1")
                .setLinkDataValue("link-1")
                .setLastSyncDate(date1)
                .setInProgress(false)
                .setHasSucceed(false);

        subject.save(link0);
        assertFalse(link0.id == 0);
        subject.save(link1);
        assertFalse(link1.id == 0);

        assertEquals(link0, subject.getById(link0.id));
        assertEquals(link1, subject.getById(link1.id));
    }

    public void testSaveUpdateExisting() throws SQLException {
        Calendar calendarDate = Calendar.getInstance();
        calendarDate.add(Calendar.HOUR, -1);
        Date date0 = calendarDate.getTime();
        calendarDate.add(Calendar.HOUR, -1);
        Date date1 = calendarDate.getTime();

        BankLink link0 = new BankLink()
                .setAccountId("account-0")
                .setAccountName("Account 0")
                .setBic("bank-0")
                .setLinkDataValue("link-0")
                .setLastSyncDate(date0)
                .setInProgress(true)
                .setHasSucceed(true);
        subject.save(link0);
        link0.setAccountId("new-account-0")
                .setAccountName("New Account 0")
                .setBic("new-bic-0")
                .setLinkDataValue("new-link-0")
                .setLastSyncDate(date1)
                .setInProgress(false)
                .setHasSucceed(false);
        subject.save(link0);

        assertEquals(link0, subject.getById(link0.id));
    }

    public void testGetAll() throws SQLException {
        Calendar calendarDate = Calendar.getInstance();
        calendarDate.add(Calendar.HOUR, -1);
        BankLink link0 = subject.save(new BankLink()
                .setAccountId("account-0")
                .setAccountName("Account 0")
                .setBic("bank-0")
                .setLinkDataValue("link-0")
                .setLastSyncDate(calendarDate.getTime())
                .setInProgress(true)
                .setHasSucceed(true));

        BankLink link1 = subject.save(new BankLink()
                .setAccountId("account-1")
                .setAccountName("Account 1")
                .setBic("bank-1")
                .setLinkDataValue("link-1"));

        BankLink link2 = subject.save(new BankLink()
                .setAccountId("account-2")
                .setAccountName("Account 2")
                .setBic("bank-2")
                .setLinkDataValue("link-2"));

        List<BankLink> all = subject.getAll();
        assertEquals(3, all.size());
        assertTrue(all.contains(link0));
        assertTrue(all.contains(link1));
        assertTrue(all.contains(link2));
    }

    public void testDeleteAll() throws SQLException {
        BankLink link0 = subject.save(new BankLink()
                .setAccountId("account-0")
                .setAccountName("Account 0")
                .setBic("bank-0")
                .setLinkDataValue("link-0"));

        BankLink link1 = subject.save(new BankLink()
                .setAccountId("account-1")
                .setAccountName("Account 1")
                .setBic("bank-1")
                .setLinkDataValue("link-1"));

        BankLink link2 = subject.save(new BankLink()
                .setAccountId("account-2")
                .setAccountName("Account 2")
                .setBic("bank-2")
                .setLinkDataValue("link-2"));

        subject.deleteAll(new long[]{link0.id, link2.id});

        List<BankLink> all = subject.getAll();
        assertEquals(1, all.size());
        assertTrue(all.contains(link1));
    }
}
