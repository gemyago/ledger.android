package com.infora.ledger.api;

import android.util.Base64;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by mye on 7/17/2015.
 */
public class DeviceSecret {
    public String secret;
    public static final String KEY_ALGORITHM = "AES";
    public static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    public SecretKeySpec keySpec() {
        return new SecretKeySpec(Base64.decode(secret, Base64.DEFAULT), KEY_ALGORITHM);
    }

    public static DeviceSecret generateNew() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGORITHM);
        keyGen.init(256);
        SecretKey secretKey = keyGen.generateKey();
        DeviceSecret deviceSecret = new DeviceSecret();
        deviceSecret.secret = Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
        return deviceSecret;
    }
}
