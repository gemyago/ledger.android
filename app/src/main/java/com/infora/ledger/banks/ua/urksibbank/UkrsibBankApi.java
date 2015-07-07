package com.infora.ledger.banks.ua.urksibbank;

import com.infora.ledger.banks.BankApi;
import com.infora.ledger.banks.FetchException;
import com.infora.ledger.banks.GetTransactionsRequest;

import java.io.IOException;
import java.util.List;

/**
 * Created by jenya on 06.07.15.
 */
public class UkrsibBankApi implements BankApi<UkrsibBankTransaction> {
    private static final String LOGIN_URL = "https://secure.my.ukrsibbank.com/web_banking/j_security_check";
    private static final String WELCOME_URL = "https://secure.my.ukrsibbank.com/web_banking/protected/welcome.jsf";
    private static final String TRANSACTIONS_FOR_DATES_URL = "https://secure.my.ukrsibbank.com/web_banking/protected/reports/sap_card_account_info.jsf";

    @Override
    public List<UkrsibBankTransaction> getTransactions(GetTransactionsRequest request) throws IOException, FetchException {
        return null;
    }
}
