package com.infora.ledger.api;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.infora.ledger.support.GoogleAuthUtilWrapper;

import java.io.IOException;

/**
 * Created by jenya on 13.03.15.
 */
public class ApiAuthenticator extends AbstractAccountAuthenticator {
    public static final String OPTION_INVALIDATE_TOKEN = "api-authenticator.invalidate-token";
    private static final String TAG = ApiAuthenticator.class.getName();
    private final Context context;
    private GoogleAuthUtilWrapper googleAuthUtil;

    public ApiAuthenticator(Context context) {
        super(context);
        this.context = context;
    }

    public GoogleAuthUtilWrapper getGoogleAuthUtil() {
        return googleAuthUtil == null ? (googleAuthUtil = new GoogleAuthUtilWrapper()) : googleAuthUtil;
    }

    public void setGoogleAuthUtil(GoogleAuthUtilWrapper googleAuthUtil) {
        this.googleAuthUtil = googleAuthUtil;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        //TODO: Implement invalidation via options
        String token;
        try {
            Log.d(TAG, "Retrieving the token for account: " + account);
            token = getGoogleAuthUtil().getToken(context, account.name);
        } catch (GoogleAuthException e) {
            Log.e(TAG, "Failed to get the token", e);
            //TODO: Implement proper handling
            throw new RuntimeException(e);
        } catch (IOException e) {
            Log.e(TAG, "Failed to get the token", e);
            //TODO: Implement proper handling
            throw new RuntimeException(e);
        }
        final Bundle result = new Bundle();
        result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        result.putString(AccountManager.KEY_AUTHTOKEN, token);
        return result;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }
}
