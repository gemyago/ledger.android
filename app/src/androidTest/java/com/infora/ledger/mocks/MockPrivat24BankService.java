package com.infora.ledger.mocks;

import com.infora.ledger.banks.ua.privatbank.Privat24BankService;
import com.infora.ledger.banks.ua.privatbank.PrivatBankException;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by mye on 12/30/2015.
 */
public class MockPrivat24BankService extends Privat24BankService {
    public OnRefreshAuthentication onRefreshAuthentication;
    public RefreshAuthenticationCall refreshAuthenticationCall;

    @Override
    public void refreshAuthentication(int bankLinkId) throws SQLException, IOException, PrivatBankException {
        if (onRefreshAuthentication != null) onRefreshAuthentication.call(bankLinkId);
        refreshAuthenticationCall = new RefreshAuthenticationCall(bankLinkId);
    }

    public interface OnRefreshAuthentication {
        void call(int bankLinkId) throws SQLException, IOException, PrivatBankException ;
    }

    public class RefreshAuthenticationCall {
        public int bankLinkId;

        public RefreshAuthenticationCall(int bankLinkId) {
            this.bankLinkId = bankLinkId;
        }
    }
}
