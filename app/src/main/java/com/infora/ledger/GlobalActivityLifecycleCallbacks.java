package com.infora.ledger;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.infora.ledger.application.di.DiUtils;
import com.infora.ledger.support.AccountManagerWrapper;

import javax.inject.Inject;

/**
 * Created by jenya on 21.03.15.
 */
public class GlobalActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = GlobalActivityLifecycleCallbacks.class.getName();
    private final Context context;

    @Inject AccountManagerWrapper accountManager;

    public GlobalActivityLifecycleCallbacks(Context context) {
        this.context = context;
        DiUtils.injector(context).inject(this);
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
        if (accountManager.getApplicationAccounts().length > 0) {
            Log.d(TAG, "Application account present. Account selection is not required.");
        } else {
            Log.d(TAG, "Application account not present. Starting login activity to select the account.");
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
