package com.infora.ledger;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.view.View;

/**
 * Created by jenya on 20.03.15.
 */
public class LoginActivityTest extends ActivityUnitTestCase<LoginActivity> {

    private DummyGooglePlayServicesUtilWrapper gpsUtil;

    public LoginActivityTest() {
        super(LoginActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        gpsUtil = new DummyGooglePlayServicesUtilWrapper();
    }

    @Override
    protected void setActivity(Activity testActivity) {
        if (testActivity != null) testActivity.setTheme(R.style.AppTheme);
        super.setActivity(testActivity);
    }

    public void testSignIn() throws Exception {
        startActivity(new Intent(), null, null);
        getActivity().setGooglePlayServicesUtilWrapper(gpsUtil);
        gpsUtil.setIsGooglePlayServicesAvailable(0);
        getActivity().signIn(new View(getActivity()));
        assertEquals(LoginActivity.REQUEST_CODE_PICK_ACCOUNT, getStartedActivityRequest());
        Intent startedActivity = getStartedActivityIntent();
        assertEquals("com.google", startedActivity.getStringArrayExtra("allowableAccountTypes")[0]);
    }

    private class DummyGooglePlayServicesUtilWrapper extends LoginActivity.GooglePlayServicesUtilWrapper {
        private int isGooglePlayServicesAvailable;

        public void setIsGooglePlayServicesAvailable(int isGooglePlayServicesAvailable) {
            this.isGooglePlayServicesAvailable = isGooglePlayServicesAvailable;
        }

        @Override
        public int isGooglePlayServicesAvailable(Context context) {
            return isGooglePlayServicesAvailable;
        }

        @Override
        public Dialog getErrorDialog(int errorCode, Activity activity, int requestCode) {
            return super.getErrorDialog(errorCode, activity, requestCode);
        }
    }
}