package com.infora.ledger.banks.ua.privatbank;

import android.util.Log;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.application.events.AddBankLinkFailed;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.banks.AddBankLinkStrategy;
import com.infora.ledger.banks.ua.privatbank.messages.AskPrivat24Otp;
import com.infora.ledger.banks.ua.privatbank.messages.AuthenticateWithOtp;
import com.infora.ledger.banks.ua.privatbank.messages.AuthenticateWithOtpFailed;
import com.infora.ledger.banks.ua.privatbank.messages.CancelAddingBankLink;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.data.DatabaseRepository;
import com.infora.ledger.data.UnitOfWork;
import com.infora.ledger.support.ObfuscatedString;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import de.greenrobot.event.EventBus;

/**
 * Created by mye on 9/11/2015.
 */
public class Privat24AddBankLinkStrategy implements AddBankLinkStrategy {
    private static final String TAG = Privat24AddBankLinkStrategy.class.getName();

    private Privat24Api.Factory apiFactory;
    private int linkId;
    private DeviceSecret deviceSecret;
    private Privat24Api api;
    private EventBus bus;
    private String operationId;
    private DatabaseContext db;

    public Privat24Api.Factory getApiFactory() {
        return apiFactory == null ? (apiFactory = new Privat24Api.Factory()) : apiFactory;
    }

    public void setApiFactory(Privat24Api.Factory apiFactory) {
        this.apiFactory = apiFactory;
    }

    public void addBankLink(EventBus bus, DatabaseContext db, BankLink bankLink, DeviceSecret deviceSecret) {
        Privat24BankLinkData linkData = bankLink.getLinkData(Privat24BankLinkData.class, deviceSecret);
        linkData.uniqueId = UUID.randomUUID().toString();
        bankLink.setLinkData(linkData, deviceSecret);
        UnitOfWork unitOfWork = db.newUnitOfWork();
        unitOfWork.addNew(bankLink);
        try {

            api = getApiFactory().createApi(linkData.uniqueId, linkData.login, linkData.password);
            operationId = api.authenticateWithPhoneAndPass();
            unitOfWork.commit();

            bus.post(new AskPrivat24Otp(bankLink.id, operationId));

            //Registering to handle AuthenticateWithOtp command
            bus.register(this);

            this.bus = bus;
            this.db = db;
            this.linkId = bankLink.id;
            this.deviceSecret = deviceSecret;
        } catch (SQLException e) {
            Log.e(TAG, "Failed to add bank link.", e);
            bus.post(new AddBankLinkFailed(e));
        } catch (PrivatBankException e) {
            Log.e(TAG, "Failed to add bank link.", e);
            bus.post(new AddBankLinkFailed(e));
        } catch (IOException e) {
            Log.e(TAG, "Failed to add bank link.", e);
            bus.post(new AddBankLinkFailed(e));
        }
    }

    public void onEventBackgroundThread(AuthenticateWithOtp command) {
        validateLinkId(command.linkId);
        if (!operationId.equals(command.operationId))
            throw new IllegalArgumentException("Wrong operationId. Expected: '" + operationId + "', was: '" + command.operationId + "'.");
        UnitOfWork unitOfWork = db.newUnitOfWork();
        try {
            String cookie = api.authenticateWithOtp(command.operationId, command.otp);
            Log.d(TAG, "Authenticated with OTP. Saving retrieved cookie...");
            BankLink bankLink = unitOfWork.getById(BankLink.class, linkId);
            Privat24BankLinkData linkData = bankLink.getLinkData(Privat24BankLinkData.class, deviceSecret);
            linkData.cookie = cookie;
            bankLink.setLinkData(linkData, deviceSecret);

            Log.d(TAG, "Getting card id of the card: " + ObfuscatedString.value(linkData.cardNumber));
            List<PrivatBankCard> cards = api.getCards(bankLink, deviceSecret);
            for (PrivatBankCard card : cards) {
                if (card.number.equals(linkData.cardNumber)) {
                    Log.d(TAG, "Card found. Card id assigned.");
                    linkData.cardid = card.cardid;
                    bankLink.setLinkData(linkData, deviceSecret);
                    break;
                }
            }
            unitOfWork.commit();
            Log.d(TAG, "Bank link added.");
            bus.unregister(this);
            bus.post(new BankLinkAdded(bankLink.accountId, bankLink.bic));
        } catch (Exception e) {
            Log.e(TAG, "Failed to authenticate with opt.", e);
            bus.post(new AuthenticateWithOtpFailed());
            return;
        }
    }

    public void onEventBackgroundThread(CancelAddingBankLink command) {
        validateLinkId(command.linkId);
        Log.d(TAG, "Canceling adding bank link '" + command.linkId + "'. This means simply deleting it.");
        DatabaseRepository<BankLink> repository = db.createRepository(BankLink.class);
        try {
            //TODO: Handle errors correctly
            repository.deleteAll(new long[]{command.linkId});
        } catch (SQLException e) {
            Log.e(TAG, "Failed to delete bank link.", e);
            throw new RuntimeException(e);
        }
        bus.unregister(this);
    }

    private void validateLinkId(int linkId) {
        if (this.linkId != linkId)
            throw new IllegalArgumentException("Wrong linkId. Expected: '" + this.linkId + "', was: '" + linkId + "'.");
    }
}
