package com.infora.ledger.banks.ua.urksibbank;

import android.util.Log;

import com.infora.ledger.banks.BankApi;
import com.infora.ledger.banks.FetchException;
import com.infora.ledger.banks.GetTransactionsRequest;
import com.infora.ledger.support.ObfuscatedString;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.CookieManager;
import java.util.List;

/**
 * Created by jenya on 06.07.15.
 */
public class UkrsibBankApi implements BankApi<UkrsibBankTransaction> {
    private static final String TAG = UkrsibBankApi.class.getName();
    private static final String LOGIN_URL = "https://secure.my.ukrsibbank.com/web_banking/j_security_check";
    private static final String WELCOME_URL = "https://secure.my.ukrsibbank.com/web_banking/protected/welcome.jsf";
    private static final String TRANSACTIONS_FOR_DATES_URL = "https://secure.my.ukrsibbank.com/web_banking/protected/reports/sap_card_account_info.jsf";


    @Override
    public List<UkrsibBankTransaction> getTransactions(GetTransactionsRequest request) throws IOException, FetchException {
        Log.i(TAG, "Fetching ukrsibbank transactions. From: " + request.startDate + ", to: " + request.endDate);

        OkHttpClient client = new OkHttpClient();
        client.setCookieHandler(new CookieManager());

        UkrsibBankLinkData linkData = request.bankLink.getLinkData(UkrsibBankLinkData.class);

        Log.d(TAG, "Authenticating...");
        Request authRequest = new Request.Builder()
                .url(LOGIN_URL)
                .post(new FormEncodingBuilder()
                        .add("j_username", linkData.login)
                        .add("j_password", linkData.password)
                        .build())
                .build();
        Response response = execute(client, authRequest);
        UkrsibBankResponseParser parser = new UkrsibBankResponseParser(response.body().byteStream());
        final String loginErrorMessage = parser.getLoginErrorMessage();
        if(loginErrorMessage != null) throw new FetchException("Authentication failed: " + loginErrorMessage);

        //Redirects are not allowed further. The redirect would mean failure.
        client.setFollowRedirects(false);

        Log.d(TAG, "Authenticated. Retrieving card accountId.");
        String accountId = parser.parseAccountId(linkData.account);
        String viewState = parser.parseViewState();

        Log.d(TAG, "Card accountId retrieved: " + ObfuscatedString.value(accountId) + ". Retrieving transactions.");

        response = execute(client, new Request.Builder()
                .url(WELCOME_URL)
                .post(new FormEncodingBuilder()
                                .add("accountId", accountId)
                                .add("javax.faces.ViewState", viewState)
                                .add("welcomeForm:_idcl", "welcomeForm:j_id_jsp_692165209_58:1:j_id_jsp_692165209_64")
                                .add("welcomeForm_SUBMIT", "1")
                                .build()
                )
                .build());
        parser = new UkrsibBankResponseParser(response.body().byteStream());
        List<UkrsibBankTransaction> transactions = parser.parseTransactions(linkData.card);
        Log.d(TAG, transactions.size() + " parsed.");
        return transactions;
    }

    private Response execute(OkHttpClient client, Request authRequest) throws IOException {
        Response response = client.newCall(authRequest).execute();
        if (!response.isSuccessful()) throw new IOException("Request failed. " + response);
        return response;
    }
}
