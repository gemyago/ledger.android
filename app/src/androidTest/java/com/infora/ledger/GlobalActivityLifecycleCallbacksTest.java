package com.infora.ledger;

import android.app.Activity;
import android.content.Intent;
import android.test.ActivityUnitTestCase;

import com.infora.ledger.application.UserEmailUtil;
import com.infora.ledger.mocks.MockSharedPreferencesProvider;

/**
 * Created by jenya on 21.03.15.
 */
public class GlobalActivityLifecycleCallbacksTest extends ActivityUnitTestCase<LoginActivity> {
    private GlobalActivityLifecycleCallbacks subject;
    private MockSharedPreferencesProvider prefsProvider;

    public GlobalActivityLifecycleCallbacksTest() {
        super(LoginActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        startActivity(new Intent(), null, null);
        prefsProvider = new MockSharedPreferencesProvider(getActivity());
        subject = new GlobalActivityLifecycleCallbacks(getActivity(), prefsProvider);
    }

    @Override
    protected void setActivity(Activity testActivity) {
        if (testActivity != null) testActivity.setTheme(R.style.AppTheme);
        super.setActivity(testActivity);
    }

    public void testOnActivityResumedDoesNothingIfThisIsLoginActivity() {
        subject.onActivityResumed(new LoginActivity());
        assertNull(getStartedActivityIntent());
    }

    public void testOnActivityResumedDoesNothingIfLoginNamePresent() {
        UserEmailUtil.saveUserEmail(prefsProvider, "test@mail.com");
        subject.onActivityResumed(new Activity());
        assertNull(getStartedActivityIntent());
    }

    public void testOnActivityResumedStartsLoginActivityIfNoLoginName() {
        subject.onActivityResumed(new Activity());
        Intent intent = getStartedActivityIntent();
        assertNotNull(intent);
        assertEquals(LoginActivity.class.getName(), intent.getComponent().getClassName());
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK, intent.getFlags());
    }
}
