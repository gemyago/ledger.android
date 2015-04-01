package com.infora.ledger.support;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.infora.ledger.BuildConfig;

import java.io.IOException;

/**
 * Created by jenya on 21.03.15.
 */
public class GoogleAuthUtilWrapper {
    private static final String TAG = GoogleAuthUtilWrapper.class.getName();

    private static final String ID_TOKEN_SCOPE = "audience:server:client_id:" + BuildConfig.GOAUTH_API_CLIENT_ID;
    public String getToken(Context context, String accountName, boolean invalidate) throws GoogleAuthException, IOException {
        Log.d(TAG, "Retrieving the id_token.");
        String token = GoogleAuthUtil.getToken(context, accountName, ID_TOKEN_SCOPE);
        if (invalidate) {
            Log.d(TAG, "Invalidate option supplied. Invalidating the token and getting a new one.");
            GoogleAuthUtil.invalidateToken(context, token);
            String newToken = GoogleAuthUtil.getToken(context, accountName, ID_TOKEN_SCOPE);
            if(newToken == token)
                throw new GoogleAuthException("Failed to invalidate the token. Same token retrieved.");
            token = newToken;
        }
        return token;
    }
}
