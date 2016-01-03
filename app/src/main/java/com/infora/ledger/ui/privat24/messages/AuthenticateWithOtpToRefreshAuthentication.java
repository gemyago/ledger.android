package com.infora.ledger.ui.privat24.messages;

import com.infora.ledger.data.BankLink;

/**
 * Created by jenya on 03.01.16.
 */
public class AuthenticateWithOtpToRefreshAuthentication {
    public String operationId;
    public String otp;
    public final BankLink bankLink;

    public AuthenticateWithOtpToRefreshAuthentication(String operationId, String otp, BankLink bankLink) {
        this.operationId = operationId;
        this.otp = otp;
        this.bankLink = bankLink;
    }

}
