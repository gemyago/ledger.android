package com.infora.ledger;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.infora.ledger.application.PendingTransactionsService;
import com.infora.ledger.application.RememberUserEmailCommand;
import com.infora.ledger.data.SharedPreferencesProvider;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 10.03.15.
 */
public class LedgerApplication extends Application {
    private static final String TAG = LedgerApplication.class.getName();
    public static final String USER_EMAIL_SETTINGS_KEY = "ledger.user-email";
    public static final String PACKAGE = "com.infora.ledger";
    private EventBus bus;

    private String userEmail;

    private SharedPreferencesProvider sharedPreferencesProvider;


    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application created");
        bus = new EventBus();

        PendingTransactionsService pendingTransactionsService = new PendingTransactionsService(getContentResolver(), bus);
        bus.register(pendingTransactionsService);
        ensureAccountChosen();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "Application terminated");
    }

    private void ensureAccountChosen() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                if (activity instanceof LoginActivity) return;
                if (getUserEmail() != null) return;
                startActivity(new Intent(LedgerApplication.this, LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

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
        });
    }

    public String getUserEmail() {
        if (userEmail == null) {
            userEmail = getSharedPreferences().getString(USER_EMAIL_SETTINGS_KEY, null);
        }
        return userEmail;
    }

    public void rememberUserEamil(String value) {
        SharedPreferences.Editor edit = getSharedPreferences().edit();
        edit.putString(USER_EMAIL_SETTINGS_KEY, value);
        edit.commit();
        userEmail = value;
    }

    public EventBus getBus() {
        return bus;
    }

    public void setBus(EventBus bus) {
        this.bus = bus;
    }

    public void onEvent(RememberUserEmailCommand cmd) {
        String value = cmd.getEmail();
        SharedPreferences.Editor edit = getSharedPreferences().edit();
        edit.putString(USER_EMAIL_SETTINGS_KEY, value);
        edit.apply();
        userEmail = value;
    }

    public SharedPreferencesProvider getSharedPreferencesProvider() {
        return sharedPreferencesProvider == null ?
                (sharedPreferencesProvider = new SharedPreferencesProvider(this)) :
                sharedPreferencesProvider;
    }

    public void setSharedPreferencesProvider(SharedPreferencesProvider sharedPreferencesProvider) {
        this.sharedPreferencesProvider = sharedPreferencesProvider;
    }

    private SharedPreferences getSharedPreferences() {
        return getSharedPreferencesProvider().getSharedPreferences(PACKAGE, Context.MODE_PRIVATE);
    }
}
