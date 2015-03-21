package com.infora.ledger.api;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.infora.ledger.LedgerApplication;

import java.io.IOException;

/**
 * Created by jenya on 17.03.15.
 */
public class GoogleIdTokensService {
    private static final String TAG = GoogleIdTokensService.class.getName();
    private static final String ID_TOKEN_SCOPE = "audience:server:client_id:127152602937-l6sn5g0albld900plkhh4b7fdjqee620.apps.googleusercontent.com";
    private Context context;

    public GoogleIdTokensService(Context context) {
        this.context = context;
    }

    public String getToken() {
        throw new RuntimeException("Not implemented");
//        LedgerApplication app = (LedgerApplication) context.getApplicationContext();
//        String email = app.getUserEmail();
//        if(email == null) throw new RuntimeException("User is not signed in");
//        try {
//            Log.d(TAG, "Retrieving the token for account: " + email);
//            return GoogleAuthUtil.getToken(context, email, ID_TOKEN_SCOPE);
//        } catch (GoogleAuthException e) {
//            return handleError(e);
//        } catch (IOException e) {
//            return handleError(e);
//        }
    }

    public void invalidateToken(String token) {
        GoogleAuthUtil.invalidateToken(context, token);
    }

    private String handleError(Exception e) {
        Log.d(TAG, "Failed to retrieve the token.", e);
        throw new RuntimeException(e);
    }
}
