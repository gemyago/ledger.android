package com.infora.ledger.mocks;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.application.DeviceSecretProvider;

/**
 * Created by mye on 7/17/2015.
 */
public class MockDeviceSecretProvider extends DeviceSecretProvider {
    private DeviceSecret secret;
    private DeviceSecret pendingSecret;

    public MockDeviceSecretProvider(DeviceSecret secret) {
        super(null, null, null);
        this.pendingSecret = secret;
    }

    @Override
    public DeviceSecret secret() {
        if (hasBeenRegistered()) return secret;
        throw new RuntimeException("The secret has not been loaded yet.");
    }

    @Override
    public boolean hasBeenRegistered() {
        return secret != null;
    }

    @Override
    public DeviceSecretProvider ensureDeviceRegistered() {
        secret = pendingSecret;
        return this;
    }
}
