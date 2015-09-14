package com.infora.ledger.banks.ua.privatbank.api;

import android.os.Build;
import android.support.annotation.NonNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.infora.ledger.banks.ua.privatbank.PrivatBankException;
import com.infora.ledger.support.LogUtil;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by mye on 9/14/2015.
 */
public class Privat24BaseApi {
    protected final OkHttpClient client;
    protected String imei;

    public Privat24BaseApi(String imei) {
        client = new OkHttpClient();
        this.imei = imei;
    }

    protected JsonObject getJson(HttpUrl url) throws IOException {
        Request httpRequest = new Request.Builder().url(url).get().build();
        Response httpResponse = client.newCall(httpRequest).execute();
        validateResponseStatus(httpResponse);
        String bodyString = httpResponse.body().string();
        try {
            return new JsonParser().parse(bodyString).getAsJsonObject();
        } catch (JsonSyntaxException ex) {
            LogUtil.e(this, "Failed to parse JSON: \n" + bodyString);
            throw ex;
        }
    }

    protected void validateResponseStatus(Response httpResponse) throws IOException {
        LogUtil.d(this, "Request completed with status: " + httpResponse.code() + ".");
        if (!httpResponse.isSuccessful()) {
            throw new IOException("Request failed. " + httpResponse);
        }
    }

    protected void ensureStatusOk(JsonObject data) throws PrivatBankException {
        if (!data.has("st")) {
            LogUtil.d(this, "Unexpected data: " + data);
            throw new PrivatBankException("Unexpected data. The st not found.");
        }
        String st = data.get("st").getAsString();
        if (!st.equals("ok")) {
            LogUtil.d(this, "Unexpected status: " + st + ". Expected: ok");
            LogUtil.d(this, "data: " + data);
            throw new PrivatBankException("Unexpected status.");
        }
    }

    protected void failWithUnexpectedNextCmd(JsonObject data, String expected_cmd) throws PrivatBankException {
        if (!data.has("nextCmd")) {
            LogUtil.d(this, "Unexpected data: " + data);
            throw new PrivatBankException("Unexpected data. The nextCmd not found.");
        }
        String nextCmd = data.get("nextCmd").getAsString();
        if (!nextCmd.equals(expected_cmd)) {
            LogUtil.d(this, "Unexpected next cmd: " + nextCmd + ". Expected: " + expected_cmd);
            LogUtil.d(this, "data: " + data);
            throw new PrivatBankException("Unexpected next cmd.");
        }
    }

    @NonNull
    protected HttpUrl.Builder createApiUrlBuilder() {
        return new HttpUrl.Builder().scheme("https").host("napi.privatbank.ua")
                .addQueryParameter("appkey", "tj6rtymr67yjrt76tyherhdbryj6r46")
                .addQueryParameter("versionOS", Build.VERSION.RELEASE)
                .addQueryParameter("version", "5.04.03")
                .addQueryParameter("imei", imei)
                .addQueryParameter("device", Build.MODEL + "%7C" + Build.MANUFACTURER);
    }
}
