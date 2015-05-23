package com.infora.ledger.banks;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created by jenya on 23.05.15.
 */
public class DefaultPrivatBankRequestSignatureBuilder implements PrivatBankRequestSignatureBuilder {
    @Override
    public String build(String data, String password) {
        byte[] md5 = DigestUtils.md5(data + password);
        byte[] sha1 = DigestUtils.sha1(new String(Hex.encodeHex(md5)));
        return new String(Hex.encodeHex(sha1));
    }
}
