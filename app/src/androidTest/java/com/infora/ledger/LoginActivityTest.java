package com.infora.ledger;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.TouchUtils;
import android.view.View;

import com.infora.ledger.application.RememberUserEmailCommand;

import de.greenrobot.event.EventBus;

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

    public void testOnActivityResultIfAccountWasPicked() {
        EventBus bus = new EventBus();
        final boolean[] commandDispatched = {false};
        bus.register(new RememberUserEmailCommandHandler() {
            @Override
            public void onEvent(RememberUserEmailCommand cmd) {
                assertEquals("test@mail.com", cmd.getEmail());
                commandDispatched[0] = true;
            }
        });
        startActivity(new Intent(), null, null);
        getActivity().setBus(bus);
        Intent data = new Intent();
        data.putExtra(AccountManager.KEY_ACCOUNT_NAME, "test@mail.com");
        getActivity().onActivityResult(LoginActivity.REQUEST_CODE_PICK_ACCOUNT, Activity.RESULT_OK, data);
        assertTrue("The command to remember user email was not dispatched.", commandDispatched[0]);

        Intent startedActivity = getStartedActivityIntent();
        assertNotNull(startedActivity);
        assertEquals(ReportActivity.class.getName(), startedActivity.getComponent().getClassName());
    }

    public void testSignIn() throws Exception {
        startActivity(new Intent(), null, null);
        getActivity().setGooglePlayServicesUtilWrapper(gpsUtil);
        gpsUtil.setIsGooglePlayServicesAvailable(0);
        View buttonView = getActivity().getWindow().findViewById(R.id.sign_in_button);
        buttonView.callOnClick();
        assertEquals(LoginActivity.REQUEST_CODE_PICK_ACCOUNT, getStartedActivityRequest());
        Intent startedActivity = getStartedActivityIntent();
        assertEquals("com.google", startedActivity.getStringArrayExtra("allowableAccountTypes")[0]);
    }

    private interface RememberUserEmailCommandHandler {
        void onEvent(RememberUserEmailCommand cmd);
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