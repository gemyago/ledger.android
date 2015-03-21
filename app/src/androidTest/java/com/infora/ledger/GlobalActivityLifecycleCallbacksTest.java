package com.infora.ledger;

import android.app.Activity;
import android.content.Intent;
import android.test.ActivityUnitTestCase;

import com.infora.ledger.application.GlobalActivityLifecycleCallbacks;
import com.infora.ledger.application.RememberUserEmailCommand;
import com.infora.ledger.mocks.MockSharedPreferencesProvider;

/**
 * Created by jenya on 21.03.15.
 */
public class GlobalActivityLifecycleCallbacksTest extends ActivityUnitTestCase<LoginActivity> {
    private LedgerApplication app;
    private GlobalActivityLifecycleCallbacks subject;

    public GlobalActivityLifecycleCallbacksTest() {
        super(LoginActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        app = new LedgerApplication();
        app.setSharedPreferencesProvider(new MockSharedPreferencesProvider(getInstrumentation().getContext()));
        subject = new GlobalActivityLifecycleCallbacks(getInstrumentation().getContext());
    }

    @Override
    protected void setActivity(Activity testActivity) {
        if (testActivity != null) testActivity.setTheme(R.style.AppTheme);
        super.setActivity(testActivity);
    }

    public void testOnActivityResumedDoesNothingIfThisIsLoginActivity() {
        startActivity(new Intent(), null, null);
        subject.onActivityResumed(getActivity());
        assertNull(getStartedActivityIntent());
    }

    public void testOnActivityResumedDoesNothingIfLoginNamePresent() {
        startActivity(new Intent(), null, null);
        subject.onActivityResumed(new Activity());
        assertNull(getStartedActivityIntent());
    }

    public void testOnActivityResumedStartsLoginActivityIfNoLoginName() {
        startActivity(new Intent(), null, null);
        app.onEvent(new RememberUserEmailCommand("test@mail.com"));
        subject.onActivityResumed(new Activity());
        assertNotNull(getStartedActivityIntent());
    }
}
