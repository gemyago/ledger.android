package com.infora.ledger.mocks;

import com.infora.ledger.support.SharedPreferencesProvider;

/**
 * Created by mye on 9/24/2015.
 */
public class MockSharedPrefsProvider extends SharedPreferencesProvider {
    public String ledgerHost;
    public boolean useManualSync;
    public boolean manuallyFetchBankLinks;

    public MockSharedPrefsProvider() {
        super(null);
    }

    @Override public String ledgerHost() {
        return ledgerHost;
    }

    @Override public boolean useManualSync() {
        return useManualSync;
    }

    @Override public boolean manuallyFetchBankLinks() {
        return manuallyFetchBankLinks;
    }
}
