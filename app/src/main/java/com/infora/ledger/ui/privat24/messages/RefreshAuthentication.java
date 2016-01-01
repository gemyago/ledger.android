package com.infora.ledger.ui.privat24.messages;

/**
 * Created by jenya on 01.01.16.
 */
public class RefreshAuthentication {
    public int bankLinkId;

    public RefreshAuthentication(int bankLinkId) {
        this.bankLinkId = bankLinkId;
    }
}
