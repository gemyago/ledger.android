package com.infora.ledger.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.infora.ledger.LedgerApplication;
import com.infora.ledger.LoginActivity;

/**
 * Created by jenya on 21.03.15.
 */
public class GlobalActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    private Context context;

    public GlobalActivityLifecycleCallbacks(Context context) {
        this.context = context;
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
        SharedPreferences prefs = context.getSharedPreferences(LedgerApplication.PACKAGE, Context.MODE_PRIVATE);
        if (prefs.getString(LedgerApplication.USER_EMAIL_SETTINGS_KEY, null) != null) return;
        context.startActivity(new Intent(context, LoginActivity.class)
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
}
