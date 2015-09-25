package com.infora.ledger;


import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.infora.ledger.application.di.DiUtils;
import com.infora.ledger.support.AccountManagerWrapper;
import com.infora.ledger.support.SyncService;

import javax.inject.Inject;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = SettingsFragment.class.getName();

    public static final String KEY_DEFAULT_ACCOUNT_ID = "default_transaction_account";
    public static final String KEY_LEDGER_HOST = "ledger_host";
    public static final String KEY_USE_MANUAL_SYNC = "use_manual_sync";
    public static final String KEY_MANUALLY_FETCH_BANK_LINKS = "manually_fetch_bank_links";

    @Inject SyncService syncService;
    @Inject AccountManagerWrapper accountManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.app_prefs);
        DiUtils.injector(getActivity()).inject(this);
    }

    @Override public void onStart() {
        super.onStart();
        Log.d(TAG, "Handling pref changes...");
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override public void onStop() {
        super.onStop();
        Log.d(TAG, "Stopping pref changes handling.");
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (KEY_USE_MANUAL_SYNC.equals(key)) {
            boolean useManualSync = sharedPreferences.getBoolean(key, false);
            Log.d(TAG, "Manual sync settings changed to: " + key + ", updating account auto sync setting accordingly.");
            syncService.setSyncAutomatically(
                    accountManager.getApplicationAccounts()[0],
                    TransactionContract.AUTHORITY,
                    !useManualSync);
        }
    }
}
