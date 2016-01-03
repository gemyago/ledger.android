package com.infora.ledger.banks;

import com.infora.ledger.TestHelper;
import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.banks.ua.privatbank.Privat24BankLinkData;
import com.infora.ledger.banks.ua.privatbank.Privat24BankService;
import com.infora.ledger.banks.ua.privatbank.PrivatBankCard;
import com.infora.ledger.banks.ua.privatbank.api.Privat24AuthApi;
import com.infora.ledger.banks.ua.privatbank.api.Privat24BankApi;
import com.infora.ledger.banks.ua.privatbank.messages.AskPrivat24OtpToCreateNewLink;
import com.infora.ledger.banks.ua.privatbank.messages.AskPrivat24OtpToRefreshAuthentication;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.mocks.MockDatabaseContext;
import com.infora.ledger.mocks.MockDatabaseRepository;
import com.infora.ledger.mocks.MockDeviceSecretProvider;
import com.infora.ledger.mocks.MockPrivat24AuthApi;
import com.infora.ledger.mocks.MockPrivat24BankApi;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.mocks.MockUnitOfWork;
import com.infora.ledger.ui.privat24.messages.AuthenticationRefreshed;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by mye on 12/30/2015.
 */
public class Privat24BankServiceTest extends TestCase {
    private EventBus bus;
    private MockDatabaseContext db;
    private DeviceSecret secret;
    private Privat24BankService subject;
    private MockDatabaseRepository<BankLink> repository;
    private BankLink bankLink;
    private Privat24BankLinkData privat24LinkData;
    private MockPrivat24AuthApi authApi;
    private MockPrivat24BankApi mockBankApi;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        bus = new EventBus();
        db = new MockDatabaseContext();
        repository = new MockDatabaseRepository<>(BankLink.class);
        db.addMockRepo(BankLink.class, repository);
        secret = DeviceSecret.generateNew();
        subject = new Privat24BankService(db, bus, new MockDeviceSecretProvider(secret).ensureDeviceRegistered());

        bankLink = new BankLink();
        bankLink.setId(TestHelper.randomInt());
        privat24LinkData = new Privat24BankLinkData()
                .setUniqueId(TestHelper.randomString("unique-id"))
                .setLogin(TestHelper.randomString("login"))
                .setPassword(TestHelper.randomString("password"))
                .setCardNumber("8867");
        bankLink.setLinkData(privat24LinkData, secret);
        repository.entitiesToGetById.add(bankLink);

        authApi = new MockPrivat24AuthApi();
        mockBankApi = new MockPrivat24BankApi();
        subject.setAuthApiFactory(new Privat24AuthApi.Factory() {
            @Override public Privat24AuthApi createApi(String uniqueId) {
                assertEquals(privat24LinkData.uniqueId, uniqueId);
                return authApi;
            }
        });
        subject.setBankApiFactory(new Privat24BankApi.Factory() {
            @Override public Privat24BankApi createApi(String uniqueId, String cookie) {
                return mockBankApi;
            }
        });
    }

    public void testAuthenticateWithOtpAndCreateNewLink() {
        authApi.onAuthenticateWithOtp = new MockPrivat24AuthApi.AuthenticateWithOtpCall() {
            @Override
            public String call(String id, String otp) {
                assertEquals("operation-1", id);
                assertEquals("9911", otp);
                return "cookie-133234";
            }
        };

        mockBankApi.onGetCards = new MockPrivat24BankApi.GetCardsCall() {
            @Override
            public List<PrivatBankCard> call() {
                ArrayList<PrivatBankCard> cards = new ArrayList<>();
                cards.add(new PrivatBankCard().setNumber("1122").setCardid("card-1122"));
                cards.add(new PrivatBankCard().setNumber("1133").setCardid("card-1133"));
                cards.add(new PrivatBankCard().setNumber(privat24LinkData.cardNumber).setCardid("card-8867"));
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
                Privat24BankLinkData savedLinkData = addedLink.getLinkData(Privat24BankLinkData.class, secret);
                assertEquals(bankLink.getLinkData(Privat24BankLinkData.class, secret).uniqueId, savedLinkData.uniqueId);
                assertEquals(privat24LinkData.password, savedLinkData.password);
                assertEquals("8867", savedLinkData.cardNumber);
                assertEquals("card-8867", savedLinkData.cardid);
            }
        });

        MockSubscriber<BankLinkAdded> addedHandler = new MockSubscriber<>(BankLinkAdded.class);
        bus.register(addedHandler);
        subject.authenticateWithOtpAndCreateNewLink("operation-1", "9911", bankLink);
        hook.assertCommitted();

        BankLinkAdded addedEvt = addedHandler.getEvent();
        assertNotNull(addedEvt);
        assertEquals(bankLink.accountId, addedEvt.accountId);
        assertEquals(bankLink.bic, addedEvt.bic);

        assertFalse(bus.isRegistered(subject));
    }

    public void testRefreshAuthentication() throws Exception {
        final String operationId = TestHelper.randomString("otp-operation-id");
        authApi.onAuthenticateWithPhoneAndPass = new MockPrivat24AuthApi.AuthenticateWithPhoneAndPassCall() {
            @Override public String call(String phone, String pass) {
                assertEquals(privat24LinkData.login, phone);
                assertEquals(privat24LinkData.password, pass);
                return operationId;
            }
        };

        MockSubscriber<AskPrivat24OtpToRefreshAuthentication> askOtpHandler = new MockSubscriber<>(AskPrivat24OtpToRefreshAuthentication.class);
        bus.register(askOtpHandler);

        subject.refreshAuthentication(bankLink.id);

        assertEquals(operationId, askOtpHandler.getEvent().operationId);
        assertSame(bankLink, askOtpHandler.getEvent().bankLink);
    }

    public void testAuthenticateWithOtpToRefreshAuthentication() throws Exception {
        final String operationId = TestHelper.randomString("otp-operation-id");
        final String otp = TestHelper.randomString("otp");
        final boolean[] apiCalled = {false};
        authApi.onAuthenticateWithOtp = new MockPrivat24AuthApi.AuthenticateWithOtpCall() {
            @Override public String call(String id, String anOtp) {
                assertEquals(operationId, id);
                assertEquals(otp, anOtp);
                apiCalled[0] = true;
                return null;
            }
        };

        MockSubscriber<AuthenticationRefreshed> successHandler =
                new MockSubscriber<>(AuthenticationRefreshed.class);
        bus.register(successHandler);

        subject.authenticateWithOtpToRefreshAuthentication(operationId, otp, bankLink);
        assertTrue(apiCalled[0]);
        assertNotNull(successHandler.getEvent());
    }
}
