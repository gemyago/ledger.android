package com.infora.ledger;

import android.accounts.Account;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.test.ActivityUnitTestCase;

import com.infora.ledger.mocks.MockAccountManagerWrapper;
import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.di.TestApplicationModule;

/**
 * Created by jenya on 21.03.15.
 */
public class GlobalActivityLifecycleCallbacksTest extends ActivityUnitTestCase<LoginActivity> {
    private GlobalActivityLifecycleCallbacks subject;
    private MockAccountManagerWrapper accountManager;

    public GlobalActivityLifecycleCallbacksTest() {
        super(LoginActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final MockLedgerApplication app = new MockLedgerApplication(getInstrumentation().getTargetContext());
        app.withInjectorModuleInit(new MockLedgerApplication.InjectorModuleInit() {
            @Override public void init(TestApplicationModule module) {
                module.accountManagerWrapper = accountManager = new MockAccountManagerWrapper(app);
            }
        });
        setActivityContext(app);
        startActivity(new Intent(), null, null);
        subject = new GlobalActivityLifecycleCallbacks(getActivity());
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

    public void testOnActivityResumedDoesNothingIfApplicationAccountIsPresent() {
        accountManager.setApplicationAccounts(new Account[]{new Account("test@domain.com", "dummy-type")});
        subject.onActivityResumed(new Activity());
        assertNull(getStartedActivityIntent());
    }

    public void testOnActivityResumedStartsLoginActivityIfNoAccounts() {
        subject.onActivityResumed(new Activity());
        Intent intent = getStartedActivityIntent();
        assertNotNull(intent);
        assertEquals(LoginActivity.class.getName(), intent.getComponent().getClassName());
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK, intent.getFlags());
    }
}
