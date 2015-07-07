package com.infora.ledger.banks.ua.privatbank;

import com.infora.ledger.banks.ua.privatbank.DefaultPrivatBankRequestSignatureBuilder;

import junit.framework.TestCase;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created by jenya on 23.05.15.
 */
public class DefaultPrivatBankRequestSignatureBuilderTest extends TestCase {

    public void testBuild() throws Exception {
        String data = "some-fake-data";
        String password = "some-fake-password";

        String sha1 = new String(Hex.encodeHex(DigestUtils.sha1(new String(Hex.encodeHex(DigestUtils.md5(data + password))))));

        DefaultPrivatBankRequestSignatureBuilder subject = new DefaultPrivatBankRequestSignatureBuilder();
        assertEquals(sha1, subject.build(data, password));
    }
}