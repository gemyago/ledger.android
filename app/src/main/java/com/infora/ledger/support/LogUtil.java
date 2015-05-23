package com.infora.ledger.support;

import android.util.Log;

/**
 * Created by jenya on 23.05.15.
 */
public class LogUtil {
    public static void d(Object owner, String message) {
        Log.d(owner.getClass().getCanonicalName(), message);
    }
}
