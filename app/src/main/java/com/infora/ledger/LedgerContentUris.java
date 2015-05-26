package com.infora.ledger;

import android.net.Uri;

/**
 * Created by jenya on 27.05.15.
 */
public class LedgerContentUris {
    public static Uri withAppendedString(Uri contentUri, String segment) {
        return contentUri.buildUpon().appendEncodedPath(String.valueOf(segment)).build();
    }
}
