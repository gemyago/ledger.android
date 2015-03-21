package com.infora.ledger.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jenya on 21.03.15.
 */
public class SharedPreferencesProvider {
    protected final Context context;

    public SharedPreferencesProvider(Context context) {
        this.context = context;
    }

    public SharedPreferences getSharedPreferences(String name, int mode) {
        return context.getSharedPreferences(name, mode);
    }
}
