package com.infora.ledger;

import android.accounts.Account;
import android.content.Context;
import android.content.SharedPreferences;
import android.test.ActivityUnitTestCase;

import com.infora.ledger.mocks.MockAccountManagerWrapper;
import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.MockSyncService;
import com.infora.ledger.mocks.di.TestApplicationModule;

import static com.infora.ledger.SettingsFragment.KEY_USE_MANUAL_SYNC;

/**
 * Created by mye on 9/25/2015.
 */
public class SettingsFragmentTest extends ActivityUnitTestCase<SettingsActivity> {

    private SettingsFragment subject;
    private MockAccountManagerWrapper accountManagerWrapper;
    private MockSyncService syncService;
    private Context context;

    public SettingsFragmentTest() {
        super(SettingsActivity.class);
    }

    @Override protected void setUp() throws Exception {
        super.setUp();

        context = getInstrumentation().getTargetContext();

        accountManagerWrapper = new MockAccountManagerWrapper(context);
        syncService = new MockSyncService();

        MockLedgerApplication app = new MockLedgerApplication(context)
                .withInjectorModuleInit(new MockLedgerApplication.InjectorModuleInit() {
                    @Override public void init(TestApplicationModule module) {
                        module.accountManagerWrapper = accountManagerWrapper;
                        module.syncService = syncService;
                    }
                });
        subject = new SettingsFragment();
        app.injector().inject(subject);
    }

    public void testOnSharedPreferenceChanged() throws Exception {
        final Account account = new Account("test", "test");
        accountManagerWrapper.setApplicationAccounts(new Account[]{account});

        final boolean[] setSyncAutomaticallyCalled = {false};
        syncService.onSetSyncAutomaticallySync = new MockSyncService.OnSetSyncAutomatically() {
            @Override public void call(Account a, String authority, boolean sync) {
                assertSame(account, a);
                assertSame(TransactionContract.AUTHORITY, authority);
                assertTrue(sync);
                setSyncAutomaticallyCalled[0] = true;
            }
        };
        SharedPreferences prefs = context.getSharedPreferences("settings-fragment-test-prefs", Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_USE_MANUAL_SYNC, false).apply();
        subject.onSharedPreferenceChanged(prefs, KEY_USE_MANUAL_SYNC);
        assertTrue(setSyncAutomaticallyCalled[0]);

        setSyncAutomaticallyCalled[0] = false;
        syncService.onSetSyncAutomaticallySync = new MockSyncService.OnSetSyncAutomatically() {
            @Override public void call(Account a, String authority, boolean sync) {
                assertFalse(sync);
                setSyncAutomaticallyCalled[0] = true;
            }
        };
        prefs.edit().putBoolean(KEY_USE_MANUAL_SYNC, true).apply();
        subject.onSharedPreferenceChanged(prefs, KEY_USE_MANUAL_SYNC);
        assertTrue(setSyncAutomaticallyCalled[0]);
    }
}