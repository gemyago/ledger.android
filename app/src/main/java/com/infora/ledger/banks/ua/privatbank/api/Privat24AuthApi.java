package com.infora.ledger.banks.ua.privatbank.api;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.infora.ledger.banks.ua.privatbank.PrivatBankException;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by mye on 9/14/2015.
 */
public class Privat24AuthApi extends Privat24BaseApi {
    private static final String TAG = Privat24AuthApi.class.getName();

    public Privat24AuthApi(String imei) {
        super(imei);
    }

    /**
     * Authenticates by phone and password. Returns an ID that should be used to authenticate with OTP
     *
     * @return
     */
    public String authenticateWithPhoneAndPass(String phone, String pass) throws IOException, PrivatBankException {
        Log.d(TAG, "Authenticating phone");
        Request httpRequest = new Request.Builder()
                .url(createApiUrlBuilder()
                        .addQueryParameter("login", phone)
                        .addPathSegment("iapi2").addPathSegment("auth_phone")
                        .build())
                .get()
                .build();
        Response httpResponse = client.newCall(httpRequest).execute();
        validateResponseStatus(httpResponse);
        JsonObject response = new JsonParser().parse(httpResponse.body().string()).getAsJsonObject();
        failWithUnexpectedNextCmd(response, "show_static_password_form");
        String id = response.get("id").getAsString();

        Log.d(TAG, "Authenticating with password");
        httpRequest = new Request.Builder()
                .url(createApiUrlBuilder()
                        .addQueryParameter("id", id)
                        .addQueryParameter("pass", pass)
                        .addPathSegment("iapi2").addPathSegment("auth_pass")
                        .build())
                .get()
                .build();
        httpResponse = client.newCall(httpRequest).execute();
        validateResponseStatus(httpResponse);
        response = new JsonParser().parse(httpResponse.body().string()).getAsJsonObject();
        failWithUnexpectedNextCmd(response, "show_otp_password_form");

        return id;
    }

    /**
     * Authenticate with OTP using the id retrieved by authenticateWithPhoneAndPass method.
     * Returns cookie that should be used with further requests
     *
     * @param id
     * @return
     */
    public String authenticateWithOtp(String id, String otp) throws IOException, PrivatBankException {
        Log.d(TAG, "Authenticating with otp");
        Request httpRequest = new Request.Builder()
                .url(createApiUrlBuilder()
                        .addQueryParameter("id", id)
                        .addQueryParameter("otp", otp)
                        .addPathSegment("iapi2").addPathSegment("auth_otp")
                        .build())
                .get()
                .build();
        Response httpResponse = client.newCall(httpRequest).execute();
        validateResponseStatus(httpResponse);
        JsonObject response = new JsonParser().parse(httpResponse.body().string()).getAsJsonObject();
        failWithUnexpectedNextCmd(response, "redirect");
        return response.get("cookie").getAsString();
    }

    public String authenticateWithPass(String pass) throws IOException, PrivatBankException {
        Log.d(TAG, "Authenticating with password to get a new cookie");
        JsonObject data = getJson(createApiUrlBuilder()
                .addQueryParameter("pass", pass)
                .addPathSegment("iapi2").addPathSegment("chpass").build());
        ensureStatusOk(data);
        Log.d(TAG, "New cookie retrieved.");
        return data.get("cookie").getAsString();
    }

    public void expireSessions() throws IOException {
        Log.d(TAG, "Expiring sessions...");
        getJson(createApiUrlBuilder()
                .addPathSegment("iapi2").addPathSegment("unpass").build());
    }

    public static class Factory {
        public Privat24AuthApi createApi(String uniqueId) {
            return new Privat24AuthApi(uniqueId);
        }
    }
}
