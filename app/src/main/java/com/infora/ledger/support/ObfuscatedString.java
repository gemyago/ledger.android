package com.infora.ledger.support;

/**
 * Created by mye on 7/8/2015.
 */
public class ObfuscatedString {
    public static String value(String originalString) {
        return originalString == null ?
                null :
                originalString.charAt(0) + "......" + originalString.charAt(originalString.length() - 1);
    }
}
