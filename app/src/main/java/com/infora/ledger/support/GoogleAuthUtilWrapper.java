package com.infora.ledger.support;

import android.content.Context;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;

import java.io.IOException;

/**
 * Created by jenya on 21.03.15.
 */
public class GoogleAuthUtilWrapper {
    private static final String ID_TOKEN_SCOPE = "audience:server:client_id:127152602937-l6sn5g0albld900plkhh4b7fdjqee620.apps.googleusercontent.com";
    public String getToken(Context context, String accountName, boolean invalidate) throws GoogleAuthException, IOException {
        String token = GoogleAuthUtil.getToken(context, accountName, ID_TOKEN_SCOPE);
        if(invalidate) {
            GoogleAuthUtil.invalidateToken(context, token);
            token = GoogleAuthUtil.getToken(context, accountName, ID_TOKEN_SCOPE);
        }
        return token;
    }
}
