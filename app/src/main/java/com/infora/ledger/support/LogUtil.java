package com.infora.ledger.support;

import android.util.Log;

/**
 * Created by jenya on 23.05.15.
 */
public class LogUtil {
    public static void d(Object owner, String message) {
        Log.d(owner.getClass().getCanonicalName(), message);
    }

    public static void e(Object owner, String message, Throwable e) {
        Log.e(owner.getClass().getCanonicalName(), message, e);
    }

    public static void e(Object owner, String message) {
        Log.e(owner.getClass().getCanonicalName(), message);
    }
}
