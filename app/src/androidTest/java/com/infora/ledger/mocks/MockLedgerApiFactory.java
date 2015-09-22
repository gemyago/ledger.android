package com.infora.ledger.mocks;

import android.accounts.Account;

import com.infora.ledger.api.LedgerApiFactory;
import com.infora.ledger.api.LedgerApi;

/**
 * Created by jenya on 11.04.15.
 */
public class MockLedgerApiFactory extends LedgerApiFactory {
    public LedgerApi createdApi;

    public MockLedgerApiFactory() {
        super(null, null, "not-existing");
    }

    public MockLedgerApiFactory(MockLedgerApi createdApi) {
        super(null, null, "not-existing");
        this.createdApi = createdApi;
    }

    public OnCreatingApi onCreatingApi;

    @Override public LedgerApi createApi() {
        return createApi(null);
    }

    @Override
    public LedgerApi createApi(Account account) {
        if(onCreatingApi == null) return createdApi;
        return onCreatingApi.call(account);
    }

    public interface OnCreatingApi {
        LedgerApi call(Account account);
    }
}
