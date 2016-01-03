package com.infora.ledger.banks.ua.privatbank;

import android.util.Log;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.application.DeviceSecretProvider;
import com.infora.ledger.application.events.AddBankLinkFailed;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.banks.ua.privatbank.api.Privat24AuthApi;
import com.infora.ledger.banks.ua.privatbank.api.Privat24BankApi;
import com.infora.ledger.banks.ua.privatbank.messages.AskPrivat24OtpToRefreshAuthentication;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.data.UnitOfWork;
import com.infora.ledger.support.ObfuscatedString;
import com.infora.ledger.ui.privat24.messages.AuthenticationRefreshed;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by mye on 12/30/2015.
 */
public class Privat24BankService {
    private static final String TAG = Privat24BankService.class.toString();

    @Inject DeviceSecretProvider secretProvider;
    @Inject DatabaseContext db;
    @Inject EventBus bus;

    private Privat24AuthApi.Factory authApiFactory;
    private Privat24BankApi.Factory bankApiFactory;

    @Inject public Privat24BankService() {
    }

    public Privat24BankService(DatabaseContext db, EventBus bus, DeviceSecretProvider secretProvider) {
        this.db = db;
        this.bus = bus;
        this.secretProvider = secretProvider;
    }

    public Privat24AuthApi.Factory getAuthApiFactory() {
        return authApiFactory == null ? (authApiFactory = new Privat24AuthApi.Factory()) : authApiFactory;
    }

    public void setAuthApiFactory(Privat24AuthApi.Factory authApiFactory) {
        this.authApiFactory = authApiFactory;
    }

    public Privat24BankApi.Factory getBankApiFactory() {
        return bankApiFactory == null ? (bankApiFactory = new Privat24BankApi.Factory()) : bankApiFactory;
    }

    public void setBankApiFactory(Privat24BankApi.Factory bankApiFactory) {
        this.bankApiFactory = bankApiFactory;
    }


    public void authenticateWithOtpAndCreateNewLink(String operationId, String otp, BankLink bankLink) {
        Log.d(TAG, "Authenticating with OTP...");
        final DeviceSecret deviceSecret = secretProvider.secret();
        Privat24BankLinkData linkData = bankLink.getLinkData(Privat24BankLinkData.class, deviceSecret);
        Privat24AuthApi authApi = getAuthApiFactory().createApi(linkData.uniqueId);
        String cookie;
        try {
            cookie = authApi.authenticateWithOtp(operationId, otp);
        } catch(IOException e) {
            Log.e(TAG, "Failed to authenticate with OTP.", e);
            bus.post(new AddBankLinkFailed(e));
            return;
        } catch(PrivatBankException e) {
            Log.e(TAG, "Failed to authenticate with OTP.", e);
            bus.post(new AddBankLinkFailed(e));
            return;
        }

        try {
            Log.d(TAG, "Getting card id of the card: " + ObfuscatedString.value(linkData.cardNumber));
            Privat24BankApi bankApi = getBankApiFactory().createApi(linkData.uniqueId, cookie);
            List<PrivatBankCard> cards = bankApi.getCards();

            for(PrivatBankCard card : cards) {
                if(card.number.equals(linkData.cardNumber)) {
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
            bus.post(new BankLinkAdded(bankLink.accountId, bankLink.bic));
        } catch(Exception e) {
            Log.e(TAG, "Failed to add bank link.", e);
            bus.post(new AddBankLinkFailed(e));
            return;
        }
    }

    public void refreshAuthentication(int bankLinkId) throws SQLException, IOException, PrivatBankException {
        Log.i(TAG, "Refreshing authentication for bank link: " + bankLinkId);
        BankLink bankLink = db.createRepository(BankLink.class).getById(bankLinkId);
        Log.d(TAG, "Retrieving corresponding bank link.");
        Privat24BankLinkData linkData = bankLink.getLinkData(Privat24BankLinkData.class, secretProvider.secret());
        Log.d(TAG, "Authenticating with phone and pass to get the OTP.");
        Privat24AuthApi authApi = getAuthApiFactory().createApi(linkData.uniqueId);
        String operationId = authApi.authenticateWithPhoneAndPass(linkData.login, linkData.password);
        Log.d(TAG, "Posting event to ask opt from the user.");
        bus.post(new AskPrivat24OtpToRefreshAuthentication(operationId, bankLink));
    }

    public void authenticateWithOtpToRefreshAuthentication(String operationId, String otp, BankLink bankLink) throws IOException, PrivatBankException {
        Log.i(TAG, "Authenticating with otp to refresh bank link: " + bankLink.id);
        Privat24BankLinkData linkData = bankLink.getLinkData(Privat24BankLinkData.class, secretProvider.secret());
        Privat24AuthApi authApi = getAuthApiFactory().createApi(linkData.uniqueId);
        authApi.authenticateWithOtp(operationId, otp);
        Log.i(TAG, "Authenticated with OTP. Authentication refreshed.");
        bus.post(new AuthenticationRefreshed());
    }
}
