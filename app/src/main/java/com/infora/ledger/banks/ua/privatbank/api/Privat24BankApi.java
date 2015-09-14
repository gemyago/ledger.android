package com.infora.ledger.banks.ua.privatbank.api;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.BankApi;
import com.infora.ledger.banks.FetchException;
import com.infora.ledger.banks.GetTransactionsRequest;
import com.infora.ledger.banks.ua.privatbank.Privat24BankLinkData;
import com.infora.ledger.banks.ua.privatbank.Privat24Transaction;
import com.infora.ledger.banks.ua.privatbank.PrivatBankCard;
import com.infora.ledger.banks.ua.privatbank.PrivatBankException;
import com.infora.ledger.support.Dates;
import com.infora.ledger.support.SystemDate;
import com.squareup.okhttp.HttpUrl;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mye on 9/10/2015.
 */
public class Privat24BankApi extends Privat24BaseApi implements BankApi<Privat24Transaction> {
    private static String TAG = Privat24BankApi.class.getName();
    private final String cookie;

    public Privat24BankApi(String imei, String cookie) {
        super(imei);
        this.cookie = cookie;
    }

    public List<PrivatBankCard> getCards() throws IOException, FetchException {
        Log.d(TAG, "Getting cards");
        JsonObject response = getJsonWithCookie(createApiUrlBuilder()
                .addPathSegment("iapi2")
                .addPathSegment("props_full"));
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
        JsonObject response = getJsonWithCookie(createApiUrlBuilder()
                .addQueryParameter("card", linkData.cardid)
                .addQueryParameter("weeks", String.valueOf(weeks))
                .addPathSegment("iapi2").addPathSegment("stats"));
        final Type listType = new TypeToken<ArrayList<Privat24Transaction>>() { }.getType();
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

    private JsonObject getJsonWithCookie(HttpUrl.Builder urlBuilder) throws IOException, PrivatBankException {
        JsonObject data = getJson(urlBuilder.addQueryParameter("cookie", cookie).build());
        ensureStatusOk(data);
        return data;
    }

    public static class Factory {
        public Privat24BankApi createApi(String uniqueId, String cookie) {
            return new Privat24BankApi(uniqueId, cookie);
        }
    }
}
