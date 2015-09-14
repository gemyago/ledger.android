package com.infora.ledger.mocks;

import com.infora.ledger.banks.ua.privatbank.PrivatBankException;
import com.infora.ledger.banks.ua.privatbank.api.Privat24AuthApi;

import java.io.IOException;

/**
 * Created by mye on 9/14/2015.
 */
public class MockPrivat24AuthApi extends Privat24AuthApi {
    public MockPrivat24AuthApi() {
        super(null);
    }

    public AuthenticateWithPhoneAndPassCall onAuthenticateWithPhoneAndPass;

    @Override
    public String authenticateWithPhoneAndPass(String phone, String pass) throws IOException, PrivatBankException {
        try {
            return onAuthenticateWithPhoneAndPass.call(phone, pass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AuthenticateWithOtpCall onAuthenticateWithOtp;

    @Override
    public String authenticateWithOtp(String id, String otp) throws IOException, PrivatBankException {
        return onAuthenticateWithOtp.call(id, otp);
    }


    public interface AuthenticateWithPhoneAndPassCall {
        String call(String phone, String pass);
    }

    public interface AuthenticateWithOtpCall {
        String call(String id, String otp);
    }
}
