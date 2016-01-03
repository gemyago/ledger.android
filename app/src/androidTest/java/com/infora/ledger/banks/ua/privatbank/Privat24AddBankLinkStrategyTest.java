package com.infora.ledger.banks.ua.privatbank;

import android.test.AndroidTestCase;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.ua.privatbank.api.Privat24AuthApi;
import com.infora.ledger.banks.ua.privatbank.messages.AskPrivat24OtpToCreateNewLink;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.mocks.MockDatabaseContext;
import com.infora.ledger.mocks.MockDeviceSecretProvider;
import com.infora.ledger.mocks.MockPrivat24AuthApi;
import com.infora.ledger.mocks.MockSubscriber;

import de.greenrobot.event.EventBus;

/**
 * Created by mye on 9/11/2015.
 */
public class Privat24AddBankLinkStrategyTest extends AndroidTestCase {
    private EventBus bus;

    private Privat24AddBankLinkStrategy subject;
    private DeviceSecret deviceSecret;
    private MockPrivat24AuthApi authApi;
    private MockDatabaseContext db;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        bus = new EventBus();
        db = new MockDatabaseContext();
        deviceSecret = DeviceSecret.generateNew();
        final MockDeviceSecretProvider secretProvider = new MockDeviceSecretProvider(deviceSecret);
        secretProvider.ensureDeviceRegistered();
        subject = new Privat24AddBankLinkStrategy(bus, db, secretProvider);
        authApi = new MockPrivat24AuthApi();
        subject.setAuthApiFactory(new Privat24AuthApi.Factory() {
            @Override
            public Privat24AuthApi createApi(String uniqueId) {
                return authApi;
            }
        });
        authApi.onAuthenticateWithPhoneAndPass = new MockPrivat24AuthApi.AuthenticateWithPhoneAndPassCall() {
            @Override
            public String call(String phone, String pass) {
                return "operation-1";
            }
        };
    }

    public void testAddBankLink() {
        BankLink bankLink = new BankLink();
        bankLink.setLinkData(new Privat24BankLinkData().setLogin("login-100").setPassword("pass-100").setCardNumber("1122"), deviceSecret);

        MockSubscriber<AskPrivat24OtpToCreateNewLink> askOtpHandler = new MockSubscriber<>(AskPrivat24OtpToCreateNewLink.class);
        bus.register(askOtpHandler);

        authApi.onAuthenticateWithPhoneAndPass = new MockPrivat24AuthApi.AuthenticateWithPhoneAndPassCall() {
            @Override
            public String call(String phone, String pass) {
                assertEquals("login-100", phone);
                assertEquals("pass-100", pass);
                return "41399320";
            }
        };

        subject.addBankLink(bankLink);

        assertEquals("41399320", askOtpHandler.getEvent().operationId);
        assertEquals(bankLink, askOtpHandler.getEvent().bankLink);
    }
}
