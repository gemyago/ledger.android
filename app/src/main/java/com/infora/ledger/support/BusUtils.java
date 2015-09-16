package com.infora.ledger.support;

import android.content.Context;

import com.infora.ledger.LedgerApplication;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 11.03.15.
 */
@Deprecated
public class BusUtils {
    public static void register(Context context) {
        getBus(context).register(context);
    }

    public static void unregister(Context context) {
        getBus(context).unregister(context);
    }

    public static void post(Context context, Object message) {
        getBus(context).post(message);
    }

    public static EventBus getBus(Context context) {
        return ((LedgerApplication) context.getApplicationContext()).getBus();
    }

    public static void setBus(Context context, EventBus bus) {
        ((LedgerApplication) context.getApplicationContext()).setBus(bus);
    }
}
