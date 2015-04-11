package com.infora.ledger.support;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jenya on 11.04.15.
 */
public class SharedPreferencesUtil {
    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        return context.getSharedPreferences(
                context.getPackageName() + "_preferences",
                Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    }
}
