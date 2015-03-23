package com.infora.ledger.mocks;

import android.accounts.Account;
import android.content.Context;
import android.os.Bundle;

import com.infora.ledger.support.AccountManagerWrapper;

/**
 * Created by jenya on 24.03.15.
 */
public class MockAccountManagerWrapper extends AccountManagerWrapper {

    private AddAccountExplicitlyArgs addAccountExplicitlyArgs;
    private Account[] applicationAccounts;

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
