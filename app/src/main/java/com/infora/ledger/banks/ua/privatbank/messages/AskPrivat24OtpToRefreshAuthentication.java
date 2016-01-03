package com.infora.ledger.banks.ua.privatbank.messages;

import com.infora.ledger.data.BankLink;

/**
 * Created by mye on 9/11/2015.
 */
public class AskPrivat24OtpToRefreshAuthentication {
    public final String operationId;
    public final BankLink bankLink;

    public AskPrivat24OtpToRefreshAuthentication(String operationId, BankLink bankLink) {
        this.operationId = operationId;
        this.bankLink = bankLink;
    }
}
