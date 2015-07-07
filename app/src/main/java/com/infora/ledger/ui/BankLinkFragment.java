package com.infora.ledger.ui;

import com.infora.ledger.banks.ua.privatbank.PrivatBankLinkData;

/**
 * Created by mye on 7/7/2015.
 */
public interface BankLinkFragment<TLinkData> {
    TLinkData getBankLinkData();

    void setBankLinkData(TLinkData linkData);

    void clearLinkData();
}
