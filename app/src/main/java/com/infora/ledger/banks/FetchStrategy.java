package com.infora.ledger.banks;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseContext;

/**
 * Created by jenya on 07.06.15.
 */
public interface FetchStrategy {
    void fetchBankTransactions(DatabaseContext db, BankLink bankLink, DeviceSecret secret) throws FetchException;
}
