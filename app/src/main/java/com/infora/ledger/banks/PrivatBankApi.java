package com.infora.ledger.banks;

import java.util.List;

/**
 * Created by jenya on 23.05.15.
 */
public class PrivatBankApi {
    private static final String API_URL = "https://api.privatbank.ua/p24api/rest_fiz";

    public List<PrivatBankTransaction> getTransactions(GetTransactionsRequest request) {
        return null;
    }
}
