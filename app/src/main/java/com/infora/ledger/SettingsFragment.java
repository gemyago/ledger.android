package com.infora.ledger;


import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment {
    public static final String KEY_LEDGER_HOST = "ledger_host";
    public static final String KEY_USE_MANUAL_SYNC = "use_manual_sync";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.app_prefs);
    }

    @Override
    public PreferenceManager getPreferenceManager() {
        return super.getPreferenceManager();
    }
}
