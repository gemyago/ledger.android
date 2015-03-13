package com.infora.ledger.api;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by jenya on 13.03.15.
 */
public class ApiAuthenticatorService extends Service {

    private ApiAuthenticator authenticator;

    @Override
    public void onCreate() {
        authenticator = new ApiAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
