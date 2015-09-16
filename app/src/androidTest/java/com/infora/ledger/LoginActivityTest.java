package com.infora.ledger;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.view.View;

import com.infora.ledger.application.commands.CreateSystemAccountCommand;
import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.mocks.di.TestApplicationModule;
import com.infora.ledger.support.GooglePlayServicesUtilWrapper;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 20.03.15.
 */
public class LoginActivityTest extends ActivityUnitTestCase<LoginActivity> {

    @Inject EventBus bus;
    private DummyGooglePlayServicesUtilWrapper gpsUtil;

    public LoginActivityTest() {
        super(LoginActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        gpsUtil = new DummyGooglePlayServicesUtilWrapper();
        MockLedgerApplication app = new MockLedgerApplication(getInstrumentation().getTargetContext())
                .withInjectorModuleInit(new MockLedgerApplication.InjectorModuleInit() {
                    @Override
                    public void init(TestApplicationModule module) {
                        module.googlePlayServicesUtilWrapper = gpsUtil;
                    }
                });
        setActivityContext(app);
        app.injector().inject(this);
    }

    @Override
    protected void setActivity(Activity testActivity) {
        if (testActivity != null) testActivity.setTheme(R.style.AppTheme);
        super.setActivity(testActivity);
    }

    public void testOnActivityResultIfAccountWasPicked() {
        MockSubscriber<CreateSystemAccountCommand> createSysAccountSubscriber = new MockSubscriber(CreateSystemAccountCommand.class);
        bus.register(createSysAccountSubscriber);
        startActivity(new Intent(), null, null);

        Intent data = new Intent();
        data.putExtra(AccountManager.KEY_ACCOUNT_NAME, "test@mail.com");
        getActivity().onActivityResult(LoginActivity.REQUEST_CODE_PICK_ACCOUNT, Activity.RESULT_OK, data);
        assertEquals(1, createSysAccountSubscriber.getEvents().size());
        assertEquals("test@mail.com", createSysAccountSubscriber.getEvent().getEmail());

        Intent startedActivity = getStartedActivityIntent();
        assertNotNull(startedActivity);
        assertEquals(ReportActivity.class.getName(), startedActivity.getComponent().getClassName());
    }

    public void testSignIn() throws Exception {
        startActivity(new Intent(), null, null);
        gpsUtil.setIsGooglePlayServicesAvailable(0);
        View buttonView = getActivity().getWindow().findViewById(R.id.sign_in_button);
        buttonView.callOnClick();
        assertEquals(LoginActivity.REQUEST_CODE_PICK_ACCOUNT, getStartedActivityRequest());
        Intent startedActivity = getStartedActivityIntent();
        assertEquals("com.google", startedActivity.getStringArrayExtra("allowableAccountTypes")[0]);
    }

    private class DummyGooglePlayServicesUtilWrapper extends GooglePlayServicesUtilWrapper {
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