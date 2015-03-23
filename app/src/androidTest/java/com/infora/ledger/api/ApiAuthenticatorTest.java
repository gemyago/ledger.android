package com.infora.ledger.api;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.test.AndroidTestCase;

import com.infora.ledger.LedgerApplication;
import com.infora.ledger.mocks.MockGoogleAuthUtilWrapper;
import com.infora.ledger.support.GoogleAuthUtilWrapper;

import junit.framework.TestCase;

/**
 * Created by jenya on 24.03.15.
 */
public class ApiAuthenticatorTest extends AndroidTestCase {

    private ApiAuthenticator subject;
    private MockGoogleAuthUtilWrapper googleAuthUtil;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        subject = new ApiAuthenticator(getContext());
        googleAuthUtil = new MockGoogleAuthUtilWrapper();
        subject.setGoogleAuthUtil(googleAuthUtil);
    }

    public void testGetAuthToken() throws Exception {
        googleAuthUtil.setToken("auth-token");
        Account account = new Account("test@domain.com", LedgerApplication.ACCOUNT_TYPE);
        Bundle result = subject.getAuthToken(null, account, null, null);
        assertEquals(account.name, result.getString(AccountManager.KEY_ACCOUNT_NAME));
        assertEquals(account.type, result.getString(AccountManager.KEY_ACCOUNT_TYPE));
        assertEquals("auth-token", result.getString(AccountManager.KEY_AUTHTOKEN));

        assertEquals(account.name, googleAuthUtil.getGetTokenArgs().getAccountName());
        assertEquals(getContext(), googleAuthUtil.getGetTokenArgs().getContext());
    }
}