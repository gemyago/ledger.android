package com.infora.ledger.banks.ua.privatbank;

import com.infora.ledger.banks.GetTransactionsRequest;
import com.infora.ledger.support.LogUtil;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;

/**
 * Created by jenya on 23.05.15.
 */
public class PrivatBankApi implements com.infora.ledger.banks.BankApi<PrivatBankTransaction> {
    private static final String API_URL = "https://my-ledger.com/pbanua2x-api/p24api/rest_fiz";
    private final OkHttpClient client;
    private final PrivatBankResponseParser responseParser;
    private final PrivatBankRequestBuilder requestBuilder;
    private MediaType XML = MediaType.parse("application/xml");

    public PrivatBankApi() {
        client = new OkHttpClient();
        responseParser = new PrivatBankResponseParser();
        requestBuilder = new PrivatBankRequestBuilder();
    }

    @Override
    public List<PrivatBankTransaction> getTransactions(GetTransactionsRequest request) throws IOException, PrivatBankException {
        LogUtil.d(this, "Fetching privatbank transactions...");
        RequestBody body = RequestBody.create(XML, requestBuilder.build(request));
        Request httpRequest = new Request.Builder().url(API_URL).post(body).build();
        Response httpResponse = client.newCall(httpRequest).execute();
        LogUtil.d(this, "Data fetched with status: " + httpResponse.code() + ".");
        if (httpResponse.isSuccessful())
            return responseParser.parseTransactions(httpResponse.body().string());
        else throw new IOException("Request failed. " + httpResponse);
    }
}
