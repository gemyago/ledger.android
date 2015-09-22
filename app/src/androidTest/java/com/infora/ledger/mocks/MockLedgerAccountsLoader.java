package com.infora.ledger.mocks;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.test.mock.MockApplication;
import android.test.mock.MockContext;

import com.infora.ledger.data.LedgerAccountsLoader;

/**
 * Created by jenya on 03.07.15.
 */
public class MockLedgerAccountsLoader extends LedgerAccountsLoader {
    public MockLedgerAccountsLoader() {
        super(new MockContext() {
            @Override
            public Context getApplicationContext() {
                return new MockApplication();
            }
        }, null);
    }

    @Override
    public Cursor loadInBackground() {
        final MatrixCursor cursor = new MatrixCursor(new String[]{COLUMN_ID, COLUMN_ACCOUNT_ID, COLUMN_NAME});
        return cursor;
    }
}
