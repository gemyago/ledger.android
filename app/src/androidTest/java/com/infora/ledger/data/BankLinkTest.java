package com.infora.ledger.data;

import android.util.Base64;

import com.google.gson.Gson;
import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.mocks.MockBankLinkData;

import junit.framework.TestCase;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

/**
 * Created by mye on 7/17/2015.
 */
public class BankLinkTest extends TestCase {
    private DeviceSecret secret;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        secret = DeviceSecret.generateNew();
    }

    public void testSetLinkData() throws Exception {
        BankLink bankLink = new BankLink();
        MockBankLinkData linkData = new MockBankLinkData("login-100", "password-100");
        bankLink.setLinkData(linkData, secret);
        byte[] rawLinkDataBytes = Base64.decode(bankLink.linkData, Base64.DEFAULT);

        //First 16 bytes (128 bits) is the IV
        byte[] iv = new byte[16];
        System.arraycopy(rawLinkDataBytes, 0, iv, 0, 16);

        byte[] actualLinkDataBytes = new byte[rawLinkDataBytes.length - 16];
        System.arraycopy(rawLinkDataBytes, 16, actualLinkDataBytes, 0, actualLinkDataBytes.length);

        Cipher cipher = Cipher.getInstance(DeviceSecret.ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secret.keySpec(), new IvParameterSpec(iv));
        String actualData = new String(cipher.doFinal(actualLinkDataBytes));
        assertEquals(new Gson().toJson(linkData), actualData);
    }

    public void testGetLinkData() throws Exception {
        IvParameterSpec iv = BankLink.generateIv();
        Cipher cipher = Cipher.getInstance(DeviceSecret.ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secret.keySpec(), iv);

        MockBankLinkData linkData = new MockBankLinkData("login-100", "password-100");

        String jsonData = new Gson().toJson(linkData);
        byte[] encryptedData = cipher.doFinal(jsonData.getBytes());

        byte[] finalData = new byte[iv.getIV().length + encryptedData.length];
        System.arraycopy(iv.getIV(), 0, finalData, 0, iv.getIV().length);
        System.arraycopy(encryptedData, 0, finalData, iv.getIV().length, encryptedData.length);

        BankLink bankLink = new BankLink().setLinkDataValue(Base64.encodeToString(finalData, Base64.DEFAULT));
        MockBankLinkData actualLinkData = bankLink.getLinkData(MockBankLinkData.class, secret);
        assertEquals(linkData, actualLinkData);
    }
}