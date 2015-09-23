package com.infora.ledger.mocks;

import com.infora.ledger.application.BankLinksService;
import com.infora.ledger.banks.FetchException;

/**
 * Created by mye on 9/23/2015.
 */
public class MockBankLinksService extends BankLinksService {
    public MockBankLinksService() {
        super(null, new MockDatabaseContext(), null);
    }

    public OnFetchAllBankLinks onFetchAllBankLinks;

    @Override public void fetchAllBankLinks() throws FetchException {
        if (onFetchAllBankLinks != null) onFetchAllBankLinks.call();
    }

    public interface OnFetchAllBankLinks {
        void call() throws FetchException;
    }
}
