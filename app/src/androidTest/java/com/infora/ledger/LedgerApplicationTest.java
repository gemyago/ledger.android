package com.infora.ledger;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import com.infora.ledger.application.RememberUserEmailCommand;
import com.infora.ledger.mocks.MockSharedPreferencesProvider;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 21.03.15.
 */
public class LedgerApplicationTest extends AndroidTestCase {

    private LedgerApplication subject;
    private MockSharedPreferencesProvider prefsProvider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        subject = new LedgerApplication();
        subject.setBus(new EventBus());
        prefsProvider = new MockSharedPreferencesProvider(getContext());
        subject.setSharedPreferencesProvider(prefsProvider);
    }

    public void testRememberUserEmailCommand() {
        subject.onEvent(new RememberUserEmailCommand("test@mail.com"));
        SharedPreferences prefs = prefsProvider.getSharedPreferences(LedgerApplication.PACKAGE, Context.MODE_PRIVATE);
        assertEquals("test@mail.com", prefs.getString(LedgerApplication.USER_EMAIL_SETTINGS_KEY, null));
        assertEquals("test@mail.com", subject.getUserEmail());
    }

    public void testGetUserEmail() {
        assertNull(subject.getUserEmail());
        SharedPreferences prefs = prefsProvider.getSharedPreferences(LedgerApplication.PACKAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LedgerApplication.USER_EMAIL_SETTINGS_KEY, "test@mail.com");
        editor.apply();
        assertEquals("test@mail.com", subject.getUserEmail());
    }

}
