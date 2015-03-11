package com.infora.ledger.api;

import android.util.Log;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

import java.io.IOException;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by jenya on 12.03.15.
 */
public class ApiAdapter {
    private static final String TAG = ApiAdapter.class.getName();

    private final RestAdapter restAdapter;
    private LedgerApi ledgerApi;
    private String authenticityToken;

    public ApiAdapter(String endpoint) {
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

    public LedgerApi getLedgerApi() {
        if (ledgerApi == null) {
            ledgerApi = restAdapter.create(LedgerApi.class);
        }
        return ledgerApi;
    }
}
