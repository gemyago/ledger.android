package com.infora.ledger.banks.ua.privatbank.api;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.BankApi;
import com.infora.ledger.banks.FetchException;
import com.infora.ledger.banks.GetTransactionsRequest;
import com.infora.ledger.banks.ua.privatbank.Privat24BankLinkData;
import com.infora.ledger.banks.ua.privatbank.Privat24Transaction;
import com.infora.ledger.banks.ua.privatbank.PrivatBankCard;
import com.infora.ledger.banks.ua.privatbank.PrivatBankException;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.support.Dates;
import com.infora.ledger.support.SystemDate;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mye on 9/10/2015.
 */
public class Privat24Api extends Privat24BaseApi implements BankApi<Privat24Transaction> {
    private static String TAG = Privat24Api.class.getName();

    private final String phone;
    private final String pass;

    public Privat24Api(String imei, String phone, String pass) {
        super(imei);
        this.phone = phone;
        this.pass = pass;
    }

    /**
     * Authenticates by phone and password. Returns an ID that should be used to authenticate with OTP
     *
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

    public List<PrivatBankCard> getCards(BankLink link, DeviceSecret secret) throws IOException, FetchException {
        Log.d(TAG, "Getting cards");
        Privat24BankLinkData linkData = link.getLinkData(Privat24BankLinkData.class, secret);
        JsonObject response = getJsonWithCookieRefresh(linkData, createApiUrlBuilder()
                .addPathSegment("iapi2").addPathSegment("props_full").build());
        Type listType = new TypeToken<ArrayList<PrivatBankCard>>() {
        }.getType();
        String cardsJson = response.getAsJsonArray("cards").toString();
        return new Gson().fromJson(cardsJson, listType);
    }

    @Override
    public List<Privat24Transaction> getTransactions(GetTransactionsRequest request, DeviceSecret secret) throws IOException, FetchException {
        //One extra week because there may be a few days diff but not whole week
        int weeks = Dates.weeksBetween(request.startDate, SystemDate.now()) + 1;

        Log.d(TAG, "Getting transactions for the past " + weeks + " weeks");


        Privat24BankLinkData linkData = request.bankLink.getLinkData(Privat24BankLinkData.class, secret);
        JsonObject response = getJsonWithCookieRefresh(linkData, createApiUrlBuilder()
                .addQueryParameter("card", linkData.cardid)
                .addQueryParameter("weeks", String.valueOf(weeks))
                .addPathSegment("iapi2").addPathSegment("stats")
                .build());
        final Type listType = new TypeToken<ArrayList<Privat24Transaction>>() {
        }.getType();
        List<Privat24Transaction> transactions = new Gson().fromJson(response.getAsJsonArray("orders").toString(), listType);

        //Filtering fetched transactions by actual dates
        String startDateString = Privat24Transaction.DateFormat.format(Dates.startOfDay(request.startDate));
        String endDateString = Privat24Transaction.DateFormat.format(Dates.endOfDay(request.endDate));
        ArrayList<Privat24Transaction> result = new ArrayList<>();
        for (Privat24Transaction transaction : transactions) {
            if (transaction.date.compareTo(startDateString) >= 0 && transaction.date.compareTo(endDateString) <= 0) {
                result.add(transaction);
            }
        }
        Log.d(TAG, transactions.size() + " fetched, " + result.size() + " filtered.");
        return result;
    }

    private JsonObject getJsonWithCookieRefresh(Privat24BankLinkData linkData, HttpUrl url) throws IOException, PrivatBankException {
        JsonObject data = getJson(url
                .newBuilder()
                .addQueryParameter("cookie", linkData.cookie)
                .build());
        if (data.get("st").getAsString().equals("fail") && data.get("err").getAsString().equals("CODE104: need auth")) {
            Log.d(TAG, "The cookie has expired. Authenticating to get a new cookie");
            data = getJson(createApiUrlBuilder()
                    .addQueryParameter("cookie", linkData.cookie)
                    .addQueryParameter("pass", pass)
                    .addPathSegment("iapi2").addPathSegment("chpass").build());
            String status = data.get("st").getAsString();
            if (!status.equals("ok")) throw new PrivatBankException("Unexpected status: " + status);
            Log.d(TAG, "New cookie retrieved.");
            linkData.cookie = data.get("cookie").getAsString();

            data = getJson(url
                    .newBuilder()
                    .addQueryParameter("cookie", linkData.cookie)
                    .build());
        }
        return data;
    }

    public static class Factory {
        public Privat24Api createApi(String uniqueId, String phone, String pass) {
            return new Privat24Api(uniqueId, phone, pass);
        }
    }
}
