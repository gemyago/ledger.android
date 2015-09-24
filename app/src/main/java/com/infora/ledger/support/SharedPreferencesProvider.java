package com.infora.ledger.support;

import android.content.Context;
import android.content.SharedPreferences;

import com.infora.ledger.SettingsFragment;

import javax.inject.Inject;

/**
 * Created by jenya on 11.04.15.
 */
public class SharedPreferencesProvider {

    private Context context;

    @Inject public SharedPreferencesProvider(Context context) {
        this.context = context;
    }

    public String ledgerHost() {
        return getDefaultSharedPreferences(context).getString(SettingsFragment.KEY_LEDGER_HOST, null);
    }

    public boolean useManualSync() {
        return getDefaultSharedPreferences(context).getBoolean(SettingsFragment.KEY_USE_MANUAL_SYNC, false);
    }

    public boolean manuallyFetchBankLinks() {
        return getDefaultSharedPreferences(context).getBoolean(SettingsFragment.KEY_MANUALLY_FETCH_BANK_LINKS, false);
    }

    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        return context.getSharedPreferences(
                context.getPackageName() + "_preferences",
                Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    }
}
