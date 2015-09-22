package com.infora.ledger.api;

import android.accounts.Account;
import android.os.Bundle;
import android.test.AndroidTestCase;

import com.infora.ledger.mocks.MockAccountManagerWrapper;
import com.infora.ledger.mocks.MockLedgerApi;

import java.util.ArrayList;
import java.util.Objects;

import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;

/**
 * Created by jenya on 11.04.15.
 */
public class LedgerApiFactoryTest extends AndroidTestCase {

    private LedgerApiFactory subject;
    private MockAccountManagerWrapper accountManager;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        accountManager = new MockAccountManagerWrapper(getContext());
        subject = new LedgerApiFactory(getContext(), accountManager, "not-existing");
    }

    public void testAuthenticateApi() throws Exception {
        MockLedgerApi api = new MockLedgerApi();
        api.setAuthenticatedAuthenticityToken(new AuthenticityToken("authenticity-token-332"));
        Account account = new Account("account-100", "test.account");
        accountManager.setGetAuthTokenCallback(new MockAccountManagerWrapper.GetAuthTokenCallback() {
            @Override
            public String onGettingToken(Account account, Bundle options) {
                assertEquals("account-100", account.name);
                assertEquals(0, options.size());
                return "google-id-token-100";
            }
        });
        subject.authenticateApi(api, account);
        assertEquals("authenticity-token-332", subject.getAuthenticityToken());
        assertEquals("google-id-token-100", api.getAuthenticatedGoogleIdToken());
    }

    public void testAuthenticateApiInvalidateAndRetry() {
        MockLedgerApi api = new MockLedgerApi();
        api.setAuthenticatedAuthenticityToken(new AuthenticityToken("authenticity-token-332"));
        Account account = new Account("account-100", "test.account");
        accountManager.setGetAuthTokenCallback(new MockAccountManagerWrapper.GetAuthTokenCallback() {
            private int attemptNumber;
            @Override
            public String onGettingToken(Account account, Bundle options) {
                assertEquals("account-100", account.name);
                if(attemptNumber == 0) {
                    assertEquals(0, options.size());
                    attemptNumber ++;
                    return "expired-google-id-token-100";
                } else if(attemptNumber == 1) {
                    assertEquals(1, options.size());
                    assertTrue(options.getBoolean(ApiAuthenticator.OPTION_INVALIDATE_TOKEN));
                    attemptNumber ++;
                    return "renewed-google-id-token-100";
                }
                throw new RuntimeException("Too much attempts");
            }
        });
        api.setAuthenticatingByTokenCallback(new MockLedgerApi.AuthenticatingByTokenCallback() {
            @Override
            public void authenticating(String googleIdToken) {
                if (Objects.equals(googleIdToken, "expired-google-id-token-100")) {
                    Response response = new Response("localhost", 401, "Unauthorized", new ArrayList<Header>(), null);
                    throw RetrofitError.httpError("localhost", response, null, null);
                }
                assertEquals("renewed-google-id-token-100", googleIdToken);
            }
        });
        subject.authenticateApi(api, account);
        assertEquals("authenticity-token-332", subject.getAuthenticityToken());
        assertEquals("renewed-google-id-token-100", api.getAuthenticatedGoogleIdToken());

    }
}