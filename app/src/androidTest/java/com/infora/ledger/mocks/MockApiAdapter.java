package com.infora.ledger.mocks;

import android.accounts.Account;

import com.infora.ledger.api.ApiAdapter;
import com.infora.ledger.api.LedgerApi;

/**
 * Created by jenya on 11.04.15.
 */
public class MockApiAdapter extends ApiAdapter {
    public LedgerApi createdApi;
    public OnAuthenticateApiCallback onAuthenticateApiCallback;

    public MockApiAdapter() {
        super(null, null, "not-existing");
    }

    @Override
    public LedgerApi createApi() {
        return createdApi;
    }

    @Override
    public void authenticateApi(LedgerApi api, Account account) {
        if(onAuthenticateApiCallback != null) onAuthenticateApiCallback.perform(api, account);
        else super.authenticateApi(api, account);
    }

    public interface OnAuthenticateApiCallback {
        void perform(LedgerApi api, Account account);
    }
}
