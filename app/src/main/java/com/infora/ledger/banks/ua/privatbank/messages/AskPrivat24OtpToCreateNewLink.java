package com.infora.ledger.banks.ua.privatbank.messages;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.data.BankLink;

/**
 * Created by mye on 9/11/2015.
 */
public class AskPrivat24OtpToCreateNewLink {
    public final String operationId;
    public final BankLink bankLink;
    public final DeviceSecret secret;

    public AskPrivat24OtpToCreateNewLink(String operationId, BankLink bankLink, DeviceSecret secret) {
        this.operationId = operationId;
        this.bankLink = bankLink;
        this.secret = secret;
    }
}
