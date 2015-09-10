package com.infora.ledger.banks.ua.privatbank;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.BankApi;
import com.infora.ledger.banks.FetchException;
import com.infora.ledger.banks.GetTransactionsRequest;

import java.io.IOException;
import java.util.List;

/**
 * Created by mye on 9/10/2015.
 */
public class Privat24Api implements BankApi<PrivatBankTransaction> {

    /**
     * Authenticates by phone and password. Returns an ID that should be used to authenticate with OTP
     * @param phone
     * @param pass
     * @return
     */
    public String authenticateWithPhoneAndPass(String phone, String pass) {
        return null;
    }

    /**
     * Authenticate with OTP using the id retrieved by authenticateWithPhoneAndPass method.
     * Returns cookie that should be used with further requests
     * @param id
     * @return
     */
    public String authenticateWithOtp(String id) {
        return null;
    }

    @Override
    public List<PrivatBankTransaction> getTransactions(GetTransactionsRequest request, DeviceSecret secret) throws IOException, FetchException {
        return null;
    }
}
