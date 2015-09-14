package com.infora.ledger.banks.ua.privatbank.messages;

/**
 * Created by mye on 9/11/2015.
 */
public class AuthenticateWithOtp {
    public String operationId;
    public String otp;

    public AuthenticateWithOtp(String operationId, String otp) {
        this.operationId = operationId;
        this.otp = otp;
    }
}
