package com.infora.ledger.banks.ua.privatbank;

import android.util.Log;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.application.events.AddBankLinkFailed;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.banks.AddBankLinkStrategy;
import com.infora.ledger.banks.ua.privatbank.api.Privat24AuthApi;
import com.infora.ledger.banks.ua.privatbank.api.Privat24BankApi;
import com.infora.ledger.banks.ua.privatbank.messages.AskPrivat24Otp;
import com.infora.ledger.banks.ua.privatbank.messages.AuthenticateWithOtp;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.data.UnitOfWork;
import com.infora.ledger.support.ObfuscatedString;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import de.greenrobot.event.EventBus;

/**
 * Created by mye on 9/11/2015.
 */
public class Privat24AddBankLinkStrategy implements AddBankLinkStrategy {
    private static final String TAG = Privat24AddBankLinkStrategy.class.getName();

    private Privat24BankApi.Factory bankApiFactory;
    private Privat24AuthApi.Factory authApiFactory;
    private DeviceSecret deviceSecret;
    private EventBus bus;
    private String operationId;
    private DatabaseContext db;
    private BankLink bankLink;

    public Privat24BankApi.Factory getBankApiFactory() {
        return bankApiFactory == null ? (bankApiFactory = new Privat24BankApi.Factory()) : bankApiFactory;
    }

    public void setBankApiFactory(Privat24BankApi.Factory bankApiFactory) {
        this.bankApiFactory = bankApiFactory;
    }

    public Privat24AuthApi.Factory getAuthApiFactory() {
        return authApiFactory == null ? (authApiFactory = new Privat24AuthApi.Factory()) : authApiFactory;
    }

    public void setAuthApiFactory(Privat24AuthApi.Factory authApiFactory) {
        this.authApiFactory = authApiFactory;
    }

    public void addBankLink(EventBus bus, DatabaseContext db, BankLink bankLink, DeviceSecret deviceSecret) {
        this.bankLink = bankLink;
        Privat24BankLinkData linkData = bankLink.getLinkData(Privat24BankLinkData.class, deviceSecret);
        if(linkData.login == null) throw new IllegalArgumentException("Login can not be null");
        if(linkData.password == null) throw new IllegalArgumentException("Password can not be null");
        if(linkData.cardNumber == null) throw new IllegalArgumentException("Card number can not be null");

        linkData.uniqueId = UUID.randomUUID().toString();
        bankLink.setLinkData(linkData, deviceSecret);
        try {
            Privat24AuthApi authApi = getAuthApiFactory().createApi(linkData.uniqueId);
            operationId = authApi.authenticateWithPhoneAndPass(linkData.login, linkData.password);

            bus.post(new AskPrivat24Otp(this.operationId));

            //Registering to handle AuthenticateWithOtp command
            bus.register(this);

            this.bus = bus;
            this.db = db;
            this.deviceSecret = deviceSecret;
        } catch (PrivatBankException e) {
            Log.e(TAG, "Failed to add bank link.", e);
            bus.post(new AddBankLinkFailed(e));
        } catch (IOException e) {
            Log.e(TAG, "Failed to add bank link.", e);
            bus.post(new AddBankLinkFailed(e));
        }
    }

    public void onEventBackgroundThread(AuthenticateWithOtp command) {
        if (!operationId.equals(command.operationId))
            throw new IllegalArgumentException("Wrong operationId. Expected: '" + operationId + "', was: '" + command.operationId + "'.");

        Log.d(TAG, "Authenticating with OTP...");
        Privat24BankLinkData linkData = bankLink.getLinkData(Privat24BankLinkData.class, deviceSecret);
        Privat24AuthApi authApi = getAuthApiFactory().createApi(linkData.uniqueId);
        String cookie;
        try {
            cookie = authApi.authenticateWithOtp(command.operationId, command.otp);
        } catch (IOException e) {
            Log.e(TAG, "Failed to authenticate with OTP.", e);
            bus.post(new AddBankLinkFailed(e));
            return;
        } catch (PrivatBankException e) {
            Log.e(TAG, "Failed to authenticate with OTP.", e);
            bus.post(new AddBankLinkFailed(e));
            return;
        }

        try {
            Log.d(TAG, "Getting card id of the card: " + ObfuscatedString.value(linkData.cardNumber));
            Privat24BankApi bankApi = getBankApiFactory().createApi(linkData.uniqueId, cookie);
            List<PrivatBankCard> cards = bankApi.getCards();

            for (PrivatBankCard card : cards) {
                if (card.number.equals(linkData.cardNumber)) {
                    Log.d(TAG, "Card found. Card id assigned.");
                    linkData.cardid = card.cardid;
                    bankLink.setLinkData(linkData, deviceSecret);
                    break;
                }
            }

            if(linkData.cardid == null) throw new PrivatBankException("Wrong card number.");

            UnitOfWork unitOfWork = db.newUnitOfWork();
            unitOfWork.addNew(bankLink);
            unitOfWork.commit();
            Log.d(TAG, "Bank link added.");
            
            bus.unregister(this);
            bus.post(new BankLinkAdded(bankLink.accountId, bankLink.bic));
        } catch (Exception e) {
            Log.e(TAG, "Failed to add bank link.", e);
            bus.post(new AddBankLinkFailed(e));
            return;
        }
    }
}
