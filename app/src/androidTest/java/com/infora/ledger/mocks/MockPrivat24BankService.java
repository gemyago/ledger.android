package com.infora.ledger.mocks;

import com.infora.ledger.banks.ua.privatbank.Privat24BankService;
import com.infora.ledger.banks.ua.privatbank.PrivatBankException;
import com.infora.ledger.data.BankLink;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by mye on 12/30/2015.
 */
public class MockPrivat24BankService extends Privat24BankService {
    public OnRefreshAuthentication onRefreshAuthentication;
    public OnAuthenticateWithOtpAndCreateNewLink onAuthenticateWithOtpAndCreateNewLink;
    public OnAuthenticateWithOtpToRefreshAuthentication onAuthenticateWithOtpToRefreshAuthentication;
    public RefreshAuthenticationCall refreshAuthenticationCall;

    @Override
    public void refreshAuthentication(int bankLinkId) throws SQLException, IOException, PrivatBankException {
        if(onRefreshAuthentication != null) onRefreshAuthentication.call(bankLinkId);
        refreshAuthenticationCall = new RefreshAuthenticationCall(bankLinkId);
    }

    @Override
    public void authenticateWithOtpAndCreateNewLink(String operationId, String otp, BankLink bankLink) {
        if(onAuthenticateWithOtpAndCreateNewLink != null)
            onAuthenticateWithOtpAndCreateNewLink.call(operationId, otp, bankLink);
    }

    @Override
    public void authenticateWithOtpToRefreshAuthentication(String operationId, String otp, BankLink bankLink) throws IOException {
        if(onAuthenticateWithOtpToRefreshAuthentication != null)
            onAuthenticateWithOtpToRefreshAuthentication.call(operationId, otp, bankLink);
    }

    public interface OnRefreshAuthentication {
        void call(int bankLinkId) throws SQLException, IOException, PrivatBankException;
    }

    public class RefreshAuthenticationCall {
        public int bankLinkId;

        public RefreshAuthenticationCall(int bankLinkId) {
            this.bankLinkId = bankLinkId;
        }
    }

    public interface OnAuthenticateWithOtpAndCreateNewLink {
        void call(String operationId, String otp, BankLink bankLink);
    }

    public interface OnAuthenticateWithOtpToRefreshAuthentication {
        void call(String operationId, String otp, BankLink bankLink) throws IOException;
    }
}
