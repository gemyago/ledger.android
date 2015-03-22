package com.infora.ledger.support;

import android.content.Context;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;

import java.io.IOException;

/**
 * Created by jenya on 21.03.15.
 */
public class GoogleAuthUtilWrapper {
    public String getToken(Context context, String accountName, String scope) throws GoogleAuthException, IOException {
        return GoogleAuthUtil.getToken(context, accountName, scope);
    }
}
