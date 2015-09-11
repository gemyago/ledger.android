package com.infora.ledger.banks;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.data.DatabaseRepository;

import de.greenrobot.event.EventBus;

/**
 * Created by mye on 9/11/2015.
 */
public interface AddBankLinkStrategy {
    void addBankLink(EventBus bus, DatabaseContext db, BankLink bankLink, DeviceSecret deviceSecret);
}
