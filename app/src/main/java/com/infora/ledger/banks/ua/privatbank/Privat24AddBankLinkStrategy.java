package com.infora.ledger.banks.ua.privatbank;

import android.util.Log;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.application.DeviceSecretProvider;
import com.infora.ledger.application.events.AddBankLinkFailed;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.banks.AddBankLinkStrategy;
import com.infora.ledger.banks.ua.privatbank.api.Privat24AuthApi;
import com.infora.ledger.banks.ua.privatbank.api.Privat24BankApi;
import com.infora.ledger.banks.ua.privatbank.messages.AskPrivat24OtpToCreateNewLink;
import com.infora.ledger.banks.ua.privatbank.messages.AuthenticateWithOtp;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.data.UnitOfWork;
import com.infora.ledger.support.ObfuscatedString;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by mye on 9/11/2015.
 */
public class Privat24AddBankLinkStrategy implements AddBankLinkStrategy {
    private static final String TAG = Privat24AddBankLinkStrategy.class.getName();

    private EventBus bus;
    private DatabaseContext db;
    private DeviceSecretProvider secretProvider;
    private Privat24BankApi.Factory bankApiFactory;
    private Privat24AuthApi.Factory authApiFactory;

    @Inject public Privat24AddBankLinkStrategy(EventBus bus, DatabaseContext db, DeviceSecretProvider secretProvider) {
        this.bus = bus;
        this.db = db;
        this.secretProvider = secretProvider;
    }

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

    public void addBankLink(BankLink bankLink) {
        final DeviceSecret deviceSecret = secretProvider.secret();
        Privat24BankLinkData linkData = bankLink.getLinkData(Privat24BankLinkData.class, deviceSecret);
        if(linkData.login == null) throw new IllegalArgumentException("Login can not be null");
        if(linkData.password == null) throw new IllegalArgumentException("Password can not be null");
        if(linkData.cardNumber == null) throw new IllegalArgumentException("Card number can not be null");

        linkData.uniqueId = UUID.randomUUID().toString();
        bankLink.setLinkData(linkData, deviceSecret);
        try {
            Privat24AuthApi authApi = getAuthApiFactory().createApi(linkData.uniqueId);
            String operationId = authApi.authenticateWithPhoneAndPass(linkData.login, linkData.password);
            bus.post(new AskPrivat24OtpToCreateNewLink(operationId, bankLink));
        } catch (PrivatBankException e) {
            Log.e(TAG, "Failed to add bank link.", e);
            bus.post(new AddBankLinkFailed(e));
        } catch (IOException e) {
            Log.e(TAG, "Failed to add bank link.", e);
            bus.post(new AddBankLinkFailed(e));
        }
    }

    public void authenticateWithOtpAndCreateNewLink(String operationId, String otp, BankLink bankLink) {
        Log.d(TAG, "Authenticating with OTP...");
        final DeviceSecret deviceSecret = secretProvider.secret();
        Privat24BankLinkData linkData = bankLink.getLinkData(Privat24BankLinkData.class, deviceSecret);
        Privat24AuthApi authApi = getAuthApiFactory().createApi(linkData.uniqueId);
        String cookie;
        try {
            cookie = authApi.authenticateWithOtp(operationId, otp);
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
