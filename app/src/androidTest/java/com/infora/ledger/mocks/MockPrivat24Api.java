package com.infora.ledger.mocks;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.FetchException;
import com.infora.ledger.banks.ua.privatbank.Privat24Api;
import com.infora.ledger.banks.ua.privatbank.PrivatBankCard;
import com.infora.ledger.banks.ua.privatbank.PrivatBankException;
import com.infora.ledger.data.BankLink;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by mye on 9/11/2015.
 */
public class MockPrivat24Api extends Privat24Api {
    public MockPrivat24Api() {
        super(null, null, null);
    }

    public Callable<String> onAuthenticateWithPhoneAndPass;

    @Override
    public String authenticateWithPhoneAndPass() throws IOException, PrivatBankException {
        try {
            return onAuthenticateWithPhoneAndPass.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AuthenticateWithOtpCall onAuthenticateWithOtp;

    @Override
    public String authenticateWithOtp(String id, String otp) throws IOException, PrivatBankException {
        return onAuthenticateWithOtp.call(id, otp);
    }

    public GetCardsCall onGetCards;

    @Override
    public List<PrivatBankCard> getCards(BankLink link, DeviceSecret secret) throws IOException, FetchException {
        return onGetCards.call(link, secret);
    }

    public interface AuthenticateWithOtpCall {
        String call(String id, String otp);
    }

    public interface GetCardsCall {
        List<PrivatBankCard> call(BankLink bankLink, DeviceSecret deviceSecret);
    }
}
