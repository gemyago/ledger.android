package com.infora.ledger.application;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import com.infora.ledger.LedgerApplication;
import com.infora.ledger.LoginActivity;

/**
 * Created by jenya on 21.03.15.
 */
public class GlobalActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    private LedgerApplication app;

    public GlobalActivityLifecycleCallbacks(LedgerApplication app) {
        this.app = app;
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
        if (app.getUserEmail() != null) return;
        app.startActivity(new Intent(app, LoginActivity.class)
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
