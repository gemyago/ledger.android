package com.infora.ledger.banks.ua.privatbank;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.BankApi;
import com.infora.ledger.banks.FetchException;
import com.infora.ledger.banks.GetTransactionsRequest;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;

/**
 * Created by mye on 9/10/2015.
 */
public class Privat24Api implements BankApi<PrivatBankTransaction> {
    private static String TAG = Privat24Api.class.getName();

    private final OkHttpClient client;
    private String imei;
    private final String phone;
    private final String pass;

    public Privat24Api(String imei, String phone, String pass) {
        this.imei = imei;
        this.phone = phone;
        this.pass = pass;
        client = new OkHttpClient();
    }

    /**
     * Authenticates by phone and password. Returns an ID that should be used to authenticate with OTP
     * @return
     */
    public String authenticateWithPhoneAndPass() throws IOException, PrivatBankException {
        Log.d(TAG, "Authenticating phone");
        Request httpRequest = new Request.Builder()
                .url(createApiUrlBuilder()
                        .addQueryParameter("login", phone)
                        .addPathSegment("iapi2").addPathSegment("auth_phone")
                        .build())
                .get()
                .build();
        Response httpResponse = client.newCall(httpRequest).execute();
        validateStatus(httpResponse);
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
        validateStatus(httpResponse);
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
        validateStatus(httpResponse);
        JsonObject response = new JsonParser().parse(httpResponse.body().string()).getAsJsonObject();
        failWithUnexpectedNextCmd(response, "redirect");
        return response.get("cookie").getAsString();
    }

    private void validateStatus(Response httpResponse) throws IOException {
        Log.d(TAG, "Request completed with status: " + httpResponse.code() + ".");
        if (!httpResponse.isSuccessful()) {
            throw new IOException("Request failed. " + httpResponse);
        }
    }

    @NonNull
    private HttpUrl.Builder createApiUrlBuilder() {
        return new HttpUrl.Builder().scheme("https").host("napi.privatbank.ua")
                .addQueryParameter("appkey", "tj6rtymr67yjrt76tyherhdbryj6r46")
                .addQueryParameter("versionOS", "4.4.2")
                .addQueryParameter("version", "5.04.03")
                .addQueryParameter("imei", imei)
                .addQueryParameter("device", "Android+SDK+built+for+x86%7Cunknown");
    }

    @Override
    public List<PrivatBankTransaction> getTransactions(GetTransactionsRequest request, DeviceSecret secret) throws IOException, FetchException {
        return null;
    }

    public void failWithUnexpectedNextCmd(JsonObject data, String expected_cmd) throws PrivatBankException {
        if(!data.has("nextCmd")) {
            Log.d(TAG, "Unexpected data: " + data);
            throw new PrivatBankException("Unexpected data. The nextCmd not found.");
        }
        String nextCmd = data.get("nextCmd").getAsString();
        if (!nextCmd.equals(expected_cmd)) {
            Log.d(TAG, "Unexpected next cmd: " + nextCmd + ". Expected: " + expected_cmd);
            Log.d(TAG, "data: " + data);
            throw new PrivatBankException("Unexpected next cmd.");
        }
    }
}
