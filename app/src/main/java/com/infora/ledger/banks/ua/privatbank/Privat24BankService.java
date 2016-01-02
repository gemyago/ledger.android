package com.infora.ledger.banks.ua.privatbank;

import android.util.Log;

import com.infora.ledger.application.DeviceSecretProvider;
import com.infora.ledger.banks.ua.privatbank.api.Privat24AuthApi;
import com.infora.ledger.banks.ua.privatbank.messages.AskPrivat24OtpToCreateNewLink;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseContext;

import java.io.IOException;
import java.sql.SQLException;

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

    public void refreshAuthentication(int bankLinkId) throws SQLException, IOException, PrivatBankException {
        Log.i(TAG, "Refreshing authentication for bank link: " + bankLinkId);
        BankLink bankLink = db.createRepository(BankLink.class).getById(bankLinkId);
        Log.d(TAG, "Retrieving corresponding bank link.");
        secretProvider.ensureDeviceRegistered(); //TODO: make sure the provider is singleton. Should be called just once.
        Privat24BankLinkData linkData = bankLink.getLinkData(Privat24BankLinkData.class, secretProvider.secret());
        Log.d(TAG, "Authenticating with phone and pass to get the OTP.");
        Privat24AuthApi authApi = getAuthApiFactory().createApi(linkData.uniqueId);
        String operationId = authApi.authenticateWithPhoneAndPass(linkData.login, linkData.password);
        Log.d(TAG, "Posting event to ask opt from the user.");
        throw new RuntimeException("Not implemented");
//        bus.post(new AskPrivat24OtpToCreateNewLink(operationId));
    }
}
