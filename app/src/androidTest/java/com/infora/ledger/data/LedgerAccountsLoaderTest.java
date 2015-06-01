package com.infora.ledger.data;

import android.database.Cursor;
import android.test.AndroidTestCase;

import com.infora.ledger.api.LedgerAccountDto;
import com.infora.ledger.mocks.MockLedgerApi;

import java.util.ArrayList;

/**
 * Created by jenya on 01.06.15.
 */
public class LedgerAccountsLoaderTest extends AndroidTestCase {

    private MockLedgerApi api;
    private LedgerAccountsLoader subject;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        api = new MockLedgerApi();
        subject = new LedgerAccountsLoader(getContext(), api);
    }

    public void testLoadInBackground() throws Exception {
        ArrayList<LedgerAccountDto> accounts = new ArrayList<>();
        accounts.add(new LedgerAccountDto("an-account-1", "An Account 1"));
        accounts.add(new LedgerAccountDto("an-account-2", "An Account 2"));
        accounts.add(new LedgerAccountDto("an-account-3", "An Account 3"));
        api.setAccounts(accounts);

        Cursor cursor = subject.loadInBackground();

        assertEquals(4, cursor.getCount());

        cursor.moveToFirst();
        assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow(LedgerAccountsLoader.COLUMN_ID)));
        assertEquals(null, cursor.getString(cursor.getColumnIndexOrThrow(LedgerAccountsLoader.COLUMN_ACCOUNT_ID)));

        int serialId = 0;
        for (LedgerAccountDto account : accounts) {
            cursor.moveToNext();
            assertEquals(++serialId, cursor.getInt(cursor.getColumnIndexOrThrow(LedgerAccountsLoader.COLUMN_ID)));
            assertEquals(account.id, cursor.getString(cursor.getColumnIndexOrThrow(LedgerAccountsLoader.COLUMN_ACCOUNT_ID)));
            assertEquals(account.name, cursor.getString(cursor.getColumnIndexOrThrow(LedgerAccountsLoader.COLUMN_NAME)));
        }
    }
}