package com.infora.ledger.mocks;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.test.mock.MockContentResolver;

import com.infora.ledger.LedgerApplication;
import com.infora.ledger.R;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 13.04.15.
 */
public class MockLedgerApplication extends LedgerApplication {
    public MockContentResolver mockContentResolver;

    public MockLedgerApplication(Context baseContext, EventBus bus) {
        attachBaseContext(baseContext);
        setBus(bus);
        setTheme(R.style.AppTheme);
    }

    @Override
    public Context getApplicationContext() {
        return this;
    }

    @Override
    public ContentResolver getContentResolver() {
        if(mockContentResolver != null) return mockContentResolver;
        return super.getContentResolver();
    }

    public MockLedgerApplication withMockContentProvider(String authority, ContentProvider provider) {
        if(mockContentResolver == null) mockContentResolver = new MockContentResolver();
        provider.attachInfo(getBaseContext(), new ProviderInfo());
        mockContentResolver.addProvider(authority, provider);
        return this;
    }
}
