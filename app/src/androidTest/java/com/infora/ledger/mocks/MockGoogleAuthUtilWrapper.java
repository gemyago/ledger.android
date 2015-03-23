package com.infora.ledger.mocks;

import android.content.Context;

import com.google.android.gms.auth.GoogleAuthException;
import com.infora.ledger.support.GoogleAuthUtilWrapper;

import java.io.IOException;

/**
 * Created by jenya on 24.03.15.
 */
public class MockGoogleAuthUtilWrapper extends GoogleAuthUtilWrapper {
    private String token;
    private GetTokenArgs getTokenArgs;

    public GetTokenArgs getGetTokenArgs() {
        return getTokenArgs;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getToken(Context context, String accountName) throws GoogleAuthException, IOException {
        getTokenArgs = new GetTokenArgs(context, accountName);
        return token;
    }

    public static class GetTokenArgs {
        private final Context context;
        private final String accountName;

        private GetTokenArgs(Context context, String accountName) {
            this.context = context;
            this.accountName = accountName;
        }

        public Context getContext() {
            return context;
        }

        public String getAccountName() {
            return accountName;
        }
    }
}
