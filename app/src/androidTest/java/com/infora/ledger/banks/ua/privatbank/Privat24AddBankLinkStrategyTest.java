package com.infora.ledger.banks.ua.privatbank;

import android.test.AndroidTestCase;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.banks.ua.privatbank.api.Privat24AuthApi;
import com.infora.ledger.banks.ua.privatbank.api.Privat24BankApi;
import com.infora.ledger.banks.ua.privatbank.messages.AskPrivat24Otp;
import com.infora.ledger.banks.ua.privatbank.messages.AuthenticateWithOtp;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.mocks.MockDatabaseContext;
import com.infora.ledger.mocks.MockPrivat24AuthApi;
import com.infora.ledger.mocks.MockPrivat24BankApi;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.mocks.MockUnitOfWork;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by mye on 9/11/2015.
 */
public class Privat24AddBankLinkStrategyTest extends AndroidTestCase {
    private EventBus bus;

    private Privat24AddBankLinkStrategy subject;
    private DeviceSecret deviceSecret;
    private MockPrivat24AuthApi authApi;
    private MockPrivat24BankApi bankApi;
    private MockDatabaseContext db;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        bus = new EventBus();
        db = new MockDatabaseContext();
        subject = new Privat24AddBankLinkStrategy();
        authApi = new MockPrivat24AuthApi();
        bankApi = new MockPrivat24BankApi();
        subject.setBankApiFactory(new Privat24BankApi.Factory() {
            @Override
            public Privat24BankApi createApi(String uniqueId, String cookie) {
                return bankApi;
            }
        });
        subject.setAuthApiFactory(new Privat24AuthApi.Factory() {
            @Override
            public Privat24AuthApi createApi(String uniqueId) {
                return authApi;
            }
        });
        deviceSecret = DeviceSecret.generateNew();

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

        MockSubscriber<AskPrivat24Otp> askOtpHandler = new MockSubscriber<>(AskPrivat24Otp.class);
        bus.register(askOtpHandler);

        authApi.onAuthenticateWithPhoneAndPass = new MockPrivat24AuthApi.AuthenticateWithPhoneAndPassCall() {
            @Override
            public String call(String phone, String pass) {
                assertEquals("login-100", phone);
                assertEquals("pass-100", pass);
                return "41399320";
            }
        };

        subject.addBankLink(bus, db, bankLink, deviceSecret);

        assertEquals("41399320", askOtpHandler.getEvent().operationId);

        assertTrue(bus.isRegistered(subject));
    }

    public void testAuthenticateWithOtp() {
        final BankLink bankLink = new BankLink().setAccountId("account-1").setBic("bic-1");
        bankLink.setLinkData(new Privat24BankLinkData()
                .setLogin("login-100")
                .setPassword("password-100")
                .setCardNumber("8867")
                , deviceSecret);
        subject.addBankLink(bus, db, bankLink, deviceSecret);

        authApi.onAuthenticateWithOtp = new MockPrivat24AuthApi.AuthenticateWithOtpCall() {
            @Override
            public String call(String id, String otp) {
                assertEquals("operation-1", id);
                assertEquals("9911", otp);
                return "cookie-133234";
            }
        };

        bankApi.onGetCards = new MockPrivat24BankApi.GetCardsCall() {
            @Override
            public List<PrivatBankCard> call() {
                ArrayList<PrivatBankCard> cards = new ArrayList<>();
                cards.add(new PrivatBankCard().setNumber("1122").setCardid("card-1122"));
                cards.add(new PrivatBankCard().setNumber("1133").setCardid("card-1133"));
                cards.add(new PrivatBankCard().setNumber("8867").setCardid("card-8867"));
                return cards;
            }
        };

        MockUnitOfWork.Hook hook = db.addUnitOfWorkHook(new MockUnitOfWork.Hook() {
            @Override
            public void onCommitted(MockUnitOfWork mockUnitOfWork) {
                assertEquals(1, mockUnitOfWork.commits.size());
                assertEquals(1, mockUnitOfWork.commits.get(0).addedEntities.size());
                BankLink addedLink = (BankLink) mockUnitOfWork.commits.get(0).addedEntities.get(0);
                assertSame(bankLink, addedLink);
                Privat24BankLinkData savedLinkData = addedLink.getLinkData(Privat24BankLinkData.class, deviceSecret);
                assertEquals(bankLink.getLinkData(Privat24BankLinkData.class, deviceSecret).uniqueId, savedLinkData.uniqueId);
                assertEquals("password-100", savedLinkData.password);
                assertEquals("8867", savedLinkData.cardNumber);
                assertEquals("card-8867", savedLinkData.cardid);
            }
        });

        MockSubscriber<BankLinkAdded> addedHandler = new MockSubscriber<>(BankLinkAdded.class);
        bus.register(addedHandler);
        subject.onEventBackgroundThread(new AuthenticateWithOtp("operation-1", "9911"));
        hook.assertCommitted();

        BankLinkAdded addedEvt = addedHandler.getEvent();
        assertNotNull(addedEvt);
        assertEquals(bankLink.accountId, addedEvt.accountId);
        assertEquals(bankLink.bic, addedEvt.bic);

        assertFalse(bus.isRegistered(subject));
    }

    public void testAuthenticateWithOtpWrongOperationId() {
        final BankLink bankLink = new BankLink().setAccountId("account-1").setBic("bic-1");
        bankLink.setLinkData(new Privat24BankLinkData().setLogin("l-1").setPassword("p-1").setCardNumber("8867"), deviceSecret);
        subject.addBankLink(bus, db, bankLink, deviceSecret);

        boolean raised = false;
        try {
            subject.onEventBackgroundThread(new AuthenticateWithOtp("wrong-operation-100", "9911"));
        } catch (IllegalArgumentException ex) {
            raised = true;
        }
        assertTrue("The exception has not been raised.", raised);
    }
}
