package com.infora.ledger.support;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import com.google.android.gms.common.GooglePlayServicesUtil;

import javax.inject.Inject;

/**
 * Created by jenya on 21.03.15.
 */
public class GooglePlayServicesUtilWrapper {
    @Inject
    public GooglePlayServicesUtilWrapper() {
    }

    public int isGooglePlayServicesAvailable(Context context) {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
    }

    public Dialog getErrorDialog(int errorCode, Activity activity, int requestCode) {
        return GooglePlayServicesUtil.getErrorDialog(errorCode, activity, requestCode);
    }
}
