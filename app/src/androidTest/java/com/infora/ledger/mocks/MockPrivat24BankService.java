package com.infora.ledger.mocks;

import com.infora.ledger.banks.ua.privatbank.Privat24BankService;

/**
 * Created by mye on 12/30/2015.
 */
public class MockPrivat24BankService extends Privat24BankService {
    public RefreshAuthenticationCall refreshAuthenticationCall;

    @Override public void refreshAuthentication(int bankLinkId) {
        refreshAuthenticationCall = new RefreshAuthenticationCall(bankLinkId);
    }

    public class RefreshAuthenticationCall {
        public int bankLinkId;

        public RefreshAuthenticationCall(int bankLinkId) {
            this.bankLinkId = bankLinkId;
        }
    }
}
