package com.infora.ledger.application;

import android.content.Context;
import android.content.SharedPreferences;

import com.infora.ledger.LedgerApplication;
import com.infora.ledger.data.SharedPreferencesProvider;

/**
 * Created by jenya on 21.03.15.
 */
public class UserEmailUtil {
    public static String getUserEmail(SharedPreferencesProvider prefsProvider) {
        SharedPreferences prefs = prefsProvider.getSharedPreferences(LedgerApplication.PACKAGE, Context.MODE_PRIVATE);
        return prefs.getString(LedgerApplication.USER_EMAIL_SETTINGS_KEY, null);
    }

    public static void saveUserEmail(SharedPreferencesProvider prefsProvider, String email) {
        SharedPreferences prefs = prefsProvider.getSharedPreferences(LedgerApplication.PACKAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LedgerApplication.USER_EMAIL_SETTINGS_KEY, email);
        editor.apply();
    }
}
