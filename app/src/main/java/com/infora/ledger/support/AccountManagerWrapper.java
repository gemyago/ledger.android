package com.infora.ledger.support;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;

import com.infora.ledger.LedgerApplication;

import java.io.IOException;

/**
 * Created by jenya on 24.03.15.
 */
public class AccountManagerWrapper {

    private final Context context;

    public AccountManagerWrapper(Context context) {
        this.context = context;
    }

    public void addAccountExplicitly(Account account, Bundle userdata) {
        AccountManager.get(context).addAccountExplicitly(account, null, userdata);
    }

    public Account[] getApplicationAccounts() {
        return AccountManager.get(context).getAccountsByType(LedgerApplication.ACCOUNT_TYPE);
    }

    public String blockingGetAuthToken(Account account) throws AuthenticatorException, OperationCanceledException, IOException {
        return AccountManager.get(context).blockingGetAuthToken(account, LedgerApplication.AUTH_TOKEN_TYPE, true);
    }
}
