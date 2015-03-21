package com.infora.ledger;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.infora.ledger.data.SharedPreferencesProvider;

/**
 * Created by jenya on 21.03.15.
 */
public class GlobalActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = GlobalActivityLifecycleCallbacks.class.getName();

    private Context context;
    private SharedPreferencesProvider prefsProvider;

    public GlobalActivityLifecycleCallbacks(Context context, SharedPreferencesProvider prefsProvider) {
        this.context = context;
        this.prefsProvider = prefsProvider;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof LoginActivity) return;
        SharedPreferences prefs = prefsProvider.getSharedPreferences(LedgerApplication.PACKAGE, Context.MODE_PRIVATE);
        String userEmail = prefs.getString(LedgerApplication.USER_EMAIL_SETTINGS_KEY, null);
        if (userEmail != null) {
            Log.d(TAG, "User email assigned. Authentication is not required.");
        } else {
            Log.d(TAG, "User email not assigned. Authentication is required. Starting login activity.");
            context.startActivity(new Intent(context, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
