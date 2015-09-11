package com.infora.ledger.banks.ua.privatbank;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.AddBankLinkStrategy;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseRepository;

import de.greenrobot.event.EventBus;

/**
 * Created by mye on 9/11/2015.
 */
public class Privat24AddBankLinkStrategy implements AddBankLinkStrategy {
    @Override
    public void addBankLink(EventBus bus, DatabaseRepository<BankLink> repository, BankLink bankLink, DeviceSecret deviceSecret) {

    }
}
