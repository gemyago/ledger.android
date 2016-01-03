package com.infora.ledger.ui.privat24.messages;

import com.infora.ledger.data.BankLink;

/**
 * Created by mye on 9/11/2015.
 */
public class AuthenticateWithOtpAndCreateNewPrivat24Link {
    public String operationId;
    public String otp;
    public final BankLink bankLink;

    public AuthenticateWithOtpAndCreateNewPrivat24Link(String operationId, String otp, BankLink bankLink) {
        this.operationId = operationId;
        this.otp = otp;
        this.bankLink = bankLink;
    }
}
