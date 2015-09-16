package com.infora.ledger.application;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

import com.infora.ledger.api.ApiAdapter;
import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.api.LedgerApi;
import com.infora.ledger.support.AccountManagerWrapper;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by mye on 7/17/2015.
 */
public class DeviceSecretProvider {
    private static final String TAG = DeviceSecretProvider.class.getName();
    private final Context context;
    private final AccountManagerWrapper accountManager;
    private DeviceSecret deviceSecret;

    @Inject @Singleton
    public DeviceSecretProvider(Context context, AccountManagerWrapper accountManager) {
        this.context = context;
        this.accountManager = accountManager;
    }

    public boolean hasBeenRegistered() {
        return deviceSecret != null;
    }

    public void ensureDeviceRegistered() {
        if (hasBeenRegistered()) return;
        Log.d(TAG, "Registering device...");
        Account[] accounts = accountManager.getApplicationAccounts();
        if (accounts.length == 0) {
            Log.w(TAG, "There are no accounts yet. Device registration skipped.");
            return;
        }
        ApiAdapter adapter = ApiAdapter.createAdapter(context, accountManager);
        LedgerApi api = adapter.createApi();

        adapter.authenticateApi(api, accounts[0]);
        deviceSecret = adapter.getDeviceSecret(api);
    }

    public DeviceSecret secret() {
        if (hasBeenRegistered()) return deviceSecret;
        throw new RuntimeException("The secret has not been loaded yet.");
    }
}
