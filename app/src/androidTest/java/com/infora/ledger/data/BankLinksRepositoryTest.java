package com.infora.ledger.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.infora.ledger.BanksContract.BankLinks;
import com.infora.ledger.DbUtils;
import com.infora.ledger.support.ObjectNotFoundException;

import java.sql.SQLException;
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
        try {
            ContentValues values = new ContentValues();
            values.put(BankLinks.COLUMN_ACCOUNT_ID, "account-100");
            values.put(BankLinks.COLUMN_BIC, "bank-100");
            values.put(BankLinks.COLUMN_LINK_DATA, "link-data-100");
            id0 = db.insert(BankLinks.TABLE_NAME, null, values);

            values = new ContentValues();
            values.put(BankLinks.COLUMN_ACCOUNT_ID, "account-101");
            values.put(BankLinks.COLUMN_BIC, "bank-101");
            values.put(BankLinks.COLUMN_LINK_DATA, "link-data-101");
            id1 = db.insert(BankLinks.TABLE_NAME, null, values);
        } finally {
            db.close();
        }

        BankLink link0 = subject.getById(id0);
        assertEquals("account-100", link0.account_id);
        assertEquals("bank-100", link0.bic);
        assertEquals("link-data-100", link0.link_data);

        BankLink link1 = subject.getById(id1);
        assertEquals("account-101", link1.account_id);
        assertEquals("bank-101", link1.bic);
        assertEquals("link-data-101", link1.link_data);

        boolean notFoundRaised = false;
        try {
            subject.getById(332234423);
        } catch (ObjectNotFoundException ex) {
            notFoundRaised = true;
        }
        assertTrue("ObjectNotFoundException not raised", notFoundRaised);
    }

    public void testSaveNew() throws SQLException {
        BankLink link0 = new BankLink()
                .setAccountId("account-0")
                .setBic("bank-0")
                .setLinkDataValue("link-0");

        BankLink link1 = new BankLink()
                .setAccountId("account-0")
                .setBic("bank-0")
                .setLinkDataValue("link-0");
        subject.save(link0);
        assertFalse(link0.id == 0);
        subject.save(link1);
        assertFalse(link1.id == 0);

        assertEquals(link0, subject.getById(link0.id));
        assertEquals(link1, subject.getById(link1.id));
    }

    public void testSaveUpdateExisting() throws SQLException {
        BankLink link0 = new BankLink()
                .setAccountId("account-0")
                .setBic("bank-0")
                .setLinkDataValue("link-0");
        subject.save(link0);
        link0.setAccountId("new-account-0").setBic("new-bic-0").setLinkDataValue("new-link-0");
        subject.save(link0);

        assertEquals(link0, subject.getById(link0.id));
    }

    public void testGetAll() throws SQLException {
        BankLink link0 = subject.save(new BankLink()
                .setAccountId("account-0")
                .setBic("bank-0")
                .setLinkDataValue("link-0"));

        BankLink link1 = subject.save(new BankLink()
                .setAccountId("account-0")
                .setBic("bank-0")
                .setLinkDataValue("link-0"));

        BankLink link2 = subject.save(new BankLink()
                .setAccountId("account-0")
                .setBic("bank-0")
                .setLinkDataValue("link-0"));

        List<BankLink> all = subject.getAll();
        assertEquals(3, all.size());
        assertTrue(all.contains(link0));
        assertTrue(all.contains(link1));
        assertTrue(all.contains(link2));
    }
}
