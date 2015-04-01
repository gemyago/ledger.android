package com.infora.ledger.support;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;

import com.infora.ledger.LedgerApplication;
import com.infora.ledger.api.ApiAuthenticator;

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

    public String getAuthToken(Account account, Bundle options) throws AuthenticatorException, OperationCanceledException, IOException {
        AccountManager accountManager = AccountManager.get(context);
        //TODO: Needs more testing. Seems like accountManager caches tokens as well.
        if(options.getBoolean(ApiAuthenticator.OPTION_INVALIDATE_TOKEN)) {
            String token = accountManager.blockingGetAuthToken(account, LedgerApplication.AUTH_TOKEN_TYPE, true);
            accountManager.invalidateAuthToken(LedgerApplication.ACCOUNT_TYPE, token);
        }
        AccountManagerFuture<Bundle> result = accountManager
                .getAuthToken(account, LedgerApplication.AUTH_TOKEN_TYPE, options, null, null, null);
        return result.getResult().getString(AccountManager.KEY_AUTHTOKEN);
    }
}
