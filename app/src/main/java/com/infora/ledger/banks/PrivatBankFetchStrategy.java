package com.infora.ledger.banks;

import com.infora.ledger.application.banks.FetchStrategy;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseContext;

import java.util.Date;

/**
 * Created by jenya on 07.06.15.
 */
public class PrivatBankFetchStrategy extends FetchStrategy {

    public PrivatBankApi getApi() {
        return null;
    }

    @Override
    public void fetchBankTransactions(DatabaseContext db, BankLink bankLink) {

    }

    private boolean isToday(Date date) {
        return false;
    }
}
