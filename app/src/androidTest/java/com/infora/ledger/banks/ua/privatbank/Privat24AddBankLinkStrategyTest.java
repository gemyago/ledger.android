package com.infora.ledger.banks.ua.privatbank;

import android.test.AndroidTestCase;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.banks.ua.privatbank.api.Privat24Api;
import com.infora.ledger.banks.ua.privatbank.messages.AskPrivat24Otp;
import com.infora.ledger.banks.ua.privatbank.messages.AuthenticateWithOtp;
import com.infora.ledger.banks.ua.privatbank.messages.CancelAddingBankLink;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.Entity;
import com.infora.ledger.mocks.MockDatabaseContext;
import com.infora.ledger.mocks.MockDatabaseRepository;
import com.infora.ledger.mocks.MockPrivat24Api;
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
    private MockPrivat24Api api;
    private MockDatabaseContext db;
    private MockUnitOfWork.Hook defaultUnitOfWorkHook;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        bus = new EventBus();
        db = new MockDatabaseContext();
        subject = new Privat24AddBankLinkStrategy();
        api = new MockPrivat24Api();
        subject.setApiFactory(new Privat24Api.Factory() {
            @Override
            public Privat24Api createApi(String uniqueId, String phone, String pass) {
                return api;
            }
        });
        deviceSecret = DeviceSecret.generateNew();

        defaultUnitOfWorkHook = new MockUnitOfWork.Hook() {
            @Override
            public <TEntity extends Entity> void onAddNew(TEntity entity) {
                ((BankLink) (entity)).id = 44321;
            }
        };
        api.onAuthenticateWithPhoneAndPass = new Callable<String>() {
            @Override
            public String call() throws Exception {
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
        bankLink.setLinkData(new Privat24BankLinkData(), deviceSecret);

        MockSubscriber<AskPrivat24Otp> askOtpHandler = new MockSubscriber<>(AskPrivat24Otp.class);
        bus.register(askOtpHandler);

        api.onAuthenticateWithPhoneAndPass = new Callable<String>() {
            @Override
            public String call() throws Exception {
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

        api.onAuthenticateWithOtp = new MockPrivat24Api.AuthenticateWithOtpCall() {
            @Override
            public String call(String id, String otp) {
                assertEquals("operation-1", id);
                assertEquals("9911", otp);
                return "cookie-133234";
            }
        };

        api.onGetCards = new MockPrivat24Api.GetCardsCall() {
            @Override
            public List<PrivatBankCard> call(BankLink l, DeviceSecret s) {
                assertSame(bankLink, l);
                assertSame(deviceSecret, s);
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
                assertEquals("cookie-133234", savedLinkData.cookie);
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
