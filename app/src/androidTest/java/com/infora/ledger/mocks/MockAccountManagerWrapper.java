package com.infora.ledger.mocks;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;

import com.infora.ledger.support.AccountManagerWrapper;

import java.io.IOException;

/**
 * Created by jenya on 24.03.15.
 */
public class MockAccountManagerWrapper extends AccountManagerWrapper {

    private AddAccountExplicitlyArgs addAccountExplicitlyArgs;
    private Account[] applicationAccounts;
    private GetAuthTokenCallback getAuthTokenCallback;

    public MockAccountManagerWrapper(Context context) {
        super(context);
        applicationAccounts = new Account[] {};
    }

    public AddAccountExplicitlyArgs getAddAccountExplicitlyArgs() {
        return addAccountExplicitlyArgs;
    }

    @Override
    public void addAccountExplicitly(Account account, Bundle userdata) {
        if (addAccountExplicitlyArgs != null) throw new RuntimeException("args already assigned");
        addAccountExplicitlyArgs = new AddAccountExplicitlyArgs(account, userdata);
    }

    @Override
    public Account[] getApplicationAccounts() {
        return applicationAccounts;
    }

    public void setApplicationAccounts(Account[] applicationAccounts) {
        this.applicationAccounts = applicationAccounts;
    }

    public void setGetAuthTokenCallback(GetAuthTokenCallback getAuthTokenCallback) {
        this.getAuthTokenCallback = getAuthTokenCallback;
    }

    @Override
    public String getAuthToken(Account account, Bundle options) throws AuthenticatorException, OperationCanceledException, IOException {
        if(getAuthTokenCallback != null) return getAuthTokenCallback.onGettingToken(account, options);
        return super.getAuthToken(account, options);
    }

    public interface GetAuthTokenCallback {
        String onGettingToken(Account account, Bundle options);
    }

    public static class AddAccountExplicitlyArgs {
        private final Account account;
        private final Bundle userdata;

        public AddAccountExplicitlyArgs(Account account, Bundle userdata) {
            this.account = account;
            this.userdata = userdata;
        }

        public Account getAccount() {
            return account;
        }

        public Bundle getUserdata() {
            return userdata;
        }
    }
}
