package com.infora.ledger.banks;

import android.util.Log;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.application.events.AddBankLinkFailed;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseRepository;

import java.sql.SQLException;

import de.greenrobot.event.EventBus;

/**
 * Created by mye on 9/11/2015.
 */
public class DefaultAddBankLinkStrategy implements AddBankLinkStrategy {
    private static final String TAG = DefaultAddBankLinkStrategy.class.getName();

    @Override
    public void addBankLink(EventBus bus, DatabaseRepository<BankLink> repository, BankLink bankLink, DeviceSecret deviceSecret) {
        try {
            repository.save(bankLink);
            bus.post(new BankLinkAdded(bankLink.accountId, bankLink.bic));
        } catch (SQLException e) {
            Log.e(TAG, "Failed to add bank link.", e);
            bus.post(new AddBankLinkFailed(e));
        }
    }
}
