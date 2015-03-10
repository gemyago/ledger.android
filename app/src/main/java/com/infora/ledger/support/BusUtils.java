package com.infora.ledger.support;

import android.app.Activity;

import com.infora.ledger.LedgerApplication;

/**
 * Created by jenya on 11.03.15.
 */
public class BusUtils {
    public static void register(Activity activity) {
        getApplication(activity).getBus().register(activity);
    }

    public static void unregister(Activity activity) {
        getApplication(activity).getBus().unregister(activity);
    }

    public static void post(Activity activity, Object message) {
        getApplication(activity).getBus().post(message);
    }

    private static LedgerApplication getApplication(Activity activity) {
        return (LedgerApplication) activity.getApplication();
    }
}
