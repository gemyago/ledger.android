package com.infora.ledger.banks;

import android.util.Log;

import com.infora.ledger.application.events.AddBankLinkFailed;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.data.UnitOfWork;

import java.sql.SQLException;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by mye on 9/11/2015.
 */
public class DefaultAddBankLinkStrategy implements AddBankLinkStrategy {
    private static final String TAG = DefaultAddBankLinkStrategy.class.getName();

    private EventBus bus;
    private DatabaseContext db;

    @Inject public DefaultAddBankLinkStrategy(EventBus bus, DatabaseContext db) {
        this.bus = bus;
        this.db = db;
    }

    @Override
    public void addBankLink(BankLink bankLink) {
        UnitOfWork unitOfWork = db.newUnitOfWork();
        unitOfWork.addNew(bankLink);
        try {
            unitOfWork.commit();
            bus.post(new BankLinkAdded(bankLink.accountId, bankLink.bic));
        } catch (SQLException e) {
            Log.e(TAG, "Failed to add bank link.", e);
            bus.post(new AddBankLinkFailed(e));
        }
    }
}
