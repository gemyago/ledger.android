package com.infora.ledger.mocks;

import android.accounts.Account;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.application.DeviceSecretProvider;

/**
 * Created by mye on 7/17/2015.
 */
public class MockDeviceSecretProvider extends DeviceSecretProvider {
    private DeviceSecret secret;

    public MockDeviceSecretProvider(DeviceSecret secret) {
        this.secret = secret;
    }

    @Override
    public DeviceSecret secret() {
        return secret;
    }

    @Override
    public void loadDeviceSecret(Account account) {
        throw new RuntimeException("Not implemented");
    }
}
