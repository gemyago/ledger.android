package com.infora.ledger.api;

import android.accounts.Account;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.os.Bundle;
import android.util.Log;

import com.infora.ledger.support.AccountManagerWrapper;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

import java.io.IOException;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;

/**
 * Created by jenya on 12.03.15.
 */
public class ApiAdapter {
    private static final String TAG = ApiAdapter.class.getName();

    private RestAdapter restAdapter;
    private LedgerApi ledgerApi;
    private String authenticityToken;
    private final AccountManagerWrapper accountManager;

    public ApiAdapter(AccountManagerWrapper accountManager, String endpoint) {
        this.accountManager = accountManager;
        OkHttpClient client = new OkHttpClient();
        final String[] apiCookie = new String[1];
        client.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());
                String cookie = response.header("Set-Cookie");
                if (cookie != null) {
                    Log.v(TAG, "Set-Cookie detected. Remembering.");
                    apiCookie[0] = cookie;
                }
                return response;
            }
        });
        restAdapter = new RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setClient(new OkClient(client))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        if (authenticityToken != null) {
                            Log.v(TAG, "Authenticity token is present: '" + authenticityToken + "'. Adding it to request.");
                            request.addHeader("X-CSRF-Token", authenticityToken);
                        }
                        if (apiCookie[0] != null) {
                            Log.v(TAG, "Cookie is present. Adding to request");
                            request.addHeader("Cookie", apiCookie[0]);
                        }
                    }
                })
                .build();
    }

    public String getAuthenticityToken() {
        return authenticityToken;
    }

    public void setAuthenticityToken(String authenticityToken) {
        this.authenticityToken = authenticityToken;
    }

    public LedgerApi createApi() {
        return restAdapter.create(LedgerApi.class);
    }

    public void authenticateApi(LedgerApi api, Account account) {
        String idToken = tryGettingToken(account, false);
        AuthenticityToken authenticityToken;
        Log.d(TAG, "Authenticating using google id_token.");
        try {
            authenticityToken = api.authenticateByIdToken(idToken);
        } catch (RetrofitError ex) {
            if (ex.getKind() == RetrofitError.Kind.HTTP && ex.getResponse().getStatus() == 401) {
                Log.e(TAG, "Authentication failed. The token might have expired. Invalidating the token and retrying.");
                idToken = tryGettingToken(account, true);
                authenticityToken = api.authenticateByIdToken(idToken);
            } else {
                Log.e(TAG, "Authentication failed. Error kind: " + ex.getKind());
                Log.e(TAG, ex.getMessage());
                throw ex;
            }
        }
        setAuthenticityToken(authenticityToken.getValue());
    }

    public LedgerApi getLedgerApi() {
        if (ledgerApi == null) {
            ledgerApi = restAdapter.create(LedgerApi.class);
        }
        return ledgerApi;
    }

    private String tryGettingToken(Account account, boolean invalidate) {
        String googleIdToken;
        try {
            Log.d(TAG, "Trying to get the token.");
            Bundle options = new Bundle();
            if (invalidate)
                options.putBoolean(ApiAuthenticator.OPTION_INVALIDATE_TOKEN, invalidate);
            googleIdToken = accountManager.getAuthToken(account, options);
        } catch (AuthenticatorException e) {
            //TODO: Implement proper handling
            throw new RuntimeException(e);
        } catch (OperationCanceledException e) {
            //TODO: Implement proper handling
            throw new RuntimeException(e);
        } catch (IOException e) {
            //TODO: Implement proper handling
            throw new RuntimeException(e);
        }
        return googleIdToken;
    }
}
