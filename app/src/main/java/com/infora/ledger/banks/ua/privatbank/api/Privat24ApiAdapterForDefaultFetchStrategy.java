package com.infora.ledger.banks.ua.privatbank.api;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.BankApi;
import com.infora.ledger.banks.FetchException;
import com.infora.ledger.banks.GetTransactionsRequest;
import com.infora.ledger.banks.ua.privatbank.Privat24BankLinkData;
import com.infora.ledger.banks.ua.privatbank.Privat24Transaction;

import java.io.IOException;
import java.util.List;

/**
 * Created by mye on 9/11/2015.
 */
public class Privat24ApiAdapterForDefaultFetchStrategy implements BankApi<Privat24Transaction> {
    @Override
    public List<Privat24Transaction> getTransactions(GetTransactionsRequest request, DeviceSecret secret) throws IOException, FetchException {
        Privat24BankLinkData linkData = request.bankLink.getLinkData(Privat24BankLinkData.class, secret);
        Privat24AuthApi authApi = new Privat24AuthApi(linkData.uniqueId);
        String cookie = authApi.authenticateWithPass(linkData.password);
        Privat24BankApi api = new Privat24BankApi(linkData.uniqueId, cookie);
        List<Privat24Transaction> transactions = api.getTransactions(request, secret);
        authApi.expireSessions();
        return transactions;
    }
}
