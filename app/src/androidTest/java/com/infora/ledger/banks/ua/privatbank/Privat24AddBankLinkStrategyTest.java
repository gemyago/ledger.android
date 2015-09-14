package com.infora.ledger.banks.ua.privatbank;

import android.test.AndroidTestCase;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.banks.ua.privatbank.api.Privat24AuthApi;
import com.infora.ledger.banks.ua.privatbank.api.Privat24BankApi;
import com.infora.ledger.banks.ua.privatbank.messages.AskPrivat24Otp;
import com.infora.ledger.banks.ua.privatbank.messages.AuthenticateWithOtp;
import com.infora.ledger.banks.ua.privatbank.messages.CancelAddingBankLink;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.Entity;
import com.infora.ledger.mocks.MockDatabaseContext;
import com.infora.ledger.mocks.MockDatabaseRepository;
import com.infora.ledger.mocks.MockPrivat24AuthApi;
import com.infora.ledger.mocks.MockPrivat24BankApi;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.mocks.MockUnitOfWork;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

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
    private MockUnitOfWork.Hook defaultUnitOfWorkHook;

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

        defaultUnitOfWorkHook = new MockUnitOfWork.Hook() {
            @Override
            public <TEntity extends Entity> void onAddNew(TEntity entity) {
                ((BankLink) (entity)).id = 44321;
            }
        };
        authApi.onAuthenticateWithPhoneAndPass = new MockPrivat24AuthApi.AuthenticateWithPhoneAndPassCall() {
            @Override
            public String call(String phone, String pass) {
                return "operation-1";
            }
        };
    }

    public void testAddBankLink() {
        MockUnitOfWork.Hook hook = db.addUnitOfWorkHook(new MockUnitOfWork.Hook() {
            @Override
            public void onCommitted(MockUnitOfWork mockUnitOfWork) {
                BankLink bankLink = (BankLink) mockUnitOfWork.commits.get(0).addedEntities.get(0);
                bankLink.id = 3322;
                Privat24BankLinkData linkData = bankLink.getLinkData(Privat24BankLinkData.class, deviceSecret);
                assertNotNull(linkData.uniqueId);

            }
        });
        BankLink bankLink = new BankLink();
        bankLink.setLinkData(new Privat24BankLinkData().setLogin("login-100").setPassword("pass-100"), deviceSecret);

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
        hook.assertCommitted();

        assertEquals(3322, askOtpHandler.getEvent().linkId);
        assertEquals("41399320", askOtpHandler.getEvent().operationId);

        assertTrue(bus.isRegistered(subject));
    }

    public void testAuthenticateWithOtp() {
        db.addUnitOfWorkHook(defaultUnitOfWorkHook);
        final BankLink bankLink = new BankLink().setAccountId("account-1").setBic("bic-1");
        bankLink.setLinkData(new Privat24BankLinkData().setCardNumber("8867"), deviceSecret);
        subject.addBankLink(bus, db, bankLink, deviceSecret);
        defaultUnitOfWorkHook.assertCommitted();

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
            public <TEntity extends Entity> TEntity onGetById(Class<TEntity> classOfEntity, int id) {
                if(id == bankLink.id) return (TEntity) bankLink;
                return null;
            }

            @Override
            public void onCommitted(MockUnitOfWork mockUnitOfWork) {
                Privat24BankLinkData savedLinkData = bankLink.getLinkData(Privat24BankLinkData.class, deviceSecret);
                assertEquals("card-8867", savedLinkData.cardid);
            }
        });

        MockSubscriber<BankLinkAdded> addedHandler = new MockSubscriber<>(BankLinkAdded.class);
        bus.register(addedHandler);
        subject.onEventBackgroundThread(new AuthenticateWithOtp(44321, "operation-1", "9911"));
        hook.assertCommitted();


        BankLinkAdded addedEvt = addedHandler.getEvent();
        assertNotNull(addedEvt);
        assertEquals(bankLink.accountId, addedEvt.accountId);
        assertEquals(bankLink.bic, addedEvt.bic);

        assertFalse(bus.isRegistered(subject));
    }

    public void testAuthenticateWithOtpWrongLinkId() {
        db.addUnitOfWorkHook(defaultUnitOfWorkHook);
        final BankLink bankLink = new BankLink().setAccountId("account-1").setBic("bic-1");
        bankLink.setLinkData(new Privat24BankLinkData().setCardNumber("8867"), deviceSecret);
        subject.addBankLink(bus, db, bankLink, deviceSecret);

        boolean raised = false;
        try {
            subject.onEventBackgroundThread(new AuthenticateWithOtp(00001, "operation-1", "9911"));
        } catch (IllegalArgumentException ex) {
            raised = true;
        }

        assertTrue("The exception has not been raised.", raised);
    }

    public void testAuthenticateWithOtpWrongOperationId() {
        db.addUnitOfWorkHook(defaultUnitOfWorkHook);
        final BankLink bankLink = new BankLink().setAccountId("account-1").setBic("bic-1");
        bankLink.setLinkData(new Privat24BankLinkData().setCardNumber("8867"), deviceSecret);
        subject.addBankLink(bus, db, bankLink, deviceSecret);

        boolean raised = false;
        try {
            subject.onEventBackgroundThread(new AuthenticateWithOtp(bankLink.id, "wrong-operation-100", "9911"));
        } catch (IllegalArgumentException ex) {
            raised = true;
        }
        assertTrue("The exception has not been raised.", raised);
    }

    public void testCancelAddingBankLink() {
        db.addUnitOfWorkHook(defaultUnitOfWorkHook);
        MockDatabaseRepository<BankLink> repository = new MockDatabaseRepository<>(BankLink.class);
        db.addMockRepo(BankLink.class, repository);

        final BankLink bankLink = new BankLink().setAccountId("account-1").setBic("bic-1");
        bankLink.setLinkData(new Privat24BankLinkData().setCardNumber("8867"), deviceSecret);
        subject.addBankLink(bus, db, bankLink, deviceSecret);
        subject.onEventBackgroundThread(new CancelAddingBankLink(bankLink.id));

        assertEquals(1, repository.deletedIds.length);
        assertEquals(bankLink.id, repository.deletedIds[0]);

        assertFalse("The bus has not been unregistered.", bus.isRegistered(subject));
    }

    public void testCancelAddingBankLinkWrongLinkId() {
        db.addUnitOfWorkHook(defaultUnitOfWorkHook);
        final BankLink bankLink = new BankLink().setAccountId("account-1").setBic("bic-1");
        bankLink.setLinkData(new Privat24BankLinkData().setCardNumber("8867"), deviceSecret);
        subject.addBankLink(bus, db, bankLink, deviceSecret);

        boolean raised = false;
        try {
            subject.onEventBackgroundThread(new CancelAddingBankLink(bankLink.id + 100));
        } catch (IllegalArgumentException ex) {
            raised = true;
        }

        assertTrue("The exception has not been raised.", raised);
    }
}
