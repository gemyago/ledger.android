package com.infora.ledger.banks.ua.urksibbank;

import android.util.Log;

import com.infora.ledger.banks.BankApi;
import com.infora.ledger.banks.FetchException;
import com.infora.ledger.banks.GetTransactionsRequest;
import com.infora.ledger.support.Dates;
import com.infora.ledger.support.ObfuscatedString;
import com.infora.ledger.support.SystemDate;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.CookieManager;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jenya on 06.07.15.
 */
public class UkrsibBankApi implements BankApi<UkrsibBankTransaction> {
    private static final String TAG = UkrsibBankApi.class.getName();
    private static final String LOGIN_URL = "https://secure.my.ukrsibbank.com/web_banking/j_security_check";
    private static final String LOGOUT_URL = "https://secure.my.ukrsibbank.com/web_banking/logout.jsp";
    private static final String WELCOME_URL = "https://secure.my.ukrsibbank.com/web_banking/protected/welcome.jsf";
    private static final String TRANSACTIONS_FOR_DATES_URL = "https://secure.my.ukrsibbank.com/web_banking/protected/reports/sap_card_account_info.jsf";


    @Override
    public List<UkrsibBankTransaction> getTransactions(GetTransactionsRequest request) throws IOException, FetchException {
        Date startDate = Dates.startOfDay(request.startDate);
        Date endDate = Dates.endOfDay(request.endDate);
        Date endOfToday = Dates.endOfDay(SystemDate.now());

        Log.i(TAG, "Fetching ukrsibbank transactions. From: " + startDate + ", to: " + endDate);

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

        Date monthAgo = Dates.monthAgo(endOfToday);
        if (!(startDate.compareTo(monthAgo) >= 0 && endDate.compareTo(endOfToday) <= 0)) {
            Log.d(TAG, "Specified dates range is now from the last month. Sending additional request to get requested transactions.");
            response = execute(client, new Request.Builder()
                    .url(TRANSACTIONS_FOR_DATES_URL)
                    .post(new FormEncodingBuilder()
                                    .add("accountId", accountId)
                                    .add("javax.faces.ViewState", parser.parseViewState())
                                    .add("cardAccountInfoForm:j_id_jsp_1610737686_38", UkrsibBankResponseParser.DATE_FORMAT.format(startDate))
                                    .add("cardAccountInfoForm:j_id_jsp_1610737686_40", UkrsibBankResponseParser.DATE_FORMAT.format(endDate))
                                    .add("cardAccountInfoForm:j_id_jsp_1610737686_43", "OK")
                                    .add("cardAccountInfoForm:reportPeriod", "0")
                                    .add("cardAccountInfoForm_SUBMIT", "1")
                                    .build()
                    )
                    .build());
            parser = new UkrsibBankResponseParser(response.body().byteStream());
        }
        tryLogout(client);
        return parseAndFilterTransactions(parser, linkData.card, startDate, endDate);
    }

    private void tryLogout(OkHttpClient client) {
        Log.d(TAG, "Logging out...");
        try {
            Response response = client.newCall(new Request.Builder().url(LOGOUT_URL).build()).execute();
            if(!(response.isSuccessful() || response.isRedirect())) {
                Log.e(TAG, "Logout failed: " + response);
            }
        } catch (IOException e) {
            Log.e(TAG, "Logout failed.", e);
        }
    }

    private List<UkrsibBankTransaction> parseAndFilterTransactions(UkrsibBankResponseParser parser, String card, Date startDate, Date endDate) throws FetchException {
        List<UkrsibBankTransaction> transactions = null;
        try {
            transactions = parser.parseTransactions(card);
        } catch (ParseException e) {
            throw new FetchException("Failed to parse transactions.", e);
        }
        ArrayList<UkrsibBankTransaction> filteredTransactions = new ArrayList<>();
        for (UkrsibBankTransaction transaction : transactions) {
            if(transaction.trandate.compareTo(startDate) >= 0 && transaction.trandate.compareTo(endDate) <= 0)
                filteredTransactions.add(transaction);
        }
        Log.d(TAG, transactions.size() + " parsed, filtered by dates: " + filteredTransactions.size());
        return filteredTransactions;
    }

    private Response execute(OkHttpClient client, Request request) throws IOException {
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Request failed. " + response);
        return response;
    }
}
