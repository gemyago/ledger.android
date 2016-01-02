package com.infora.ledger.banks;

import com.infora.ledger.TestHelper;
import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.ua.privatbank.Privat24BankLinkData;
import com.infora.ledger.banks.ua.privatbank.Privat24BankService;
import com.infora.ledger.banks.ua.privatbank.api.Privat24AuthApi;
import com.infora.ledger.banks.ua.privatbank.messages.AskPrivat24OtpToCreateNewLink;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.mocks.MockDatabaseContext;
import com.infora.ledger.mocks.MockDatabaseRepository;
import com.infora.ledger.mocks.MockDeviceSecretProvider;
import com.infora.ledger.mocks.MockPrivat24AuthApi;
import com.infora.ledger.mocks.MockSubscriber;

import junit.framework.TestCase;

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

    @Override
    public void setUp() throws Exception {
        super.setUp();
        bus = new EventBus();
        db = new MockDatabaseContext();
        repository = new MockDatabaseRepository<>(BankLink.class);
        db.addMockRepo(BankLink.class, repository);
        secret = DeviceSecret.generateNew();
        subject = new Privat24BankService(db, bus, new MockDeviceSecretProvider(secret));

        bankLink = new BankLink();
        bankLink.setId(TestHelper.randomInt());
        privat24LinkData = new Privat24BankLinkData()
                .setUniqueId(TestHelper.randomString("unique-id"))
                .setLogin(TestHelper.randomString("login"))
                .setPassword(TestHelper.randomString("password"));
        bankLink.setLinkData(privat24LinkData, secret);
        repository.entitiesToGetById.add(bankLink);

        authApi = new MockPrivat24AuthApi();
        subject.setAuthApiFactory(new Privat24AuthApi.Factory() {
            @Override public Privat24AuthApi createApi(String uniqueId) {
                assertEquals(privat24LinkData.uniqueId, uniqueId);
                return authApi;
            }
        });
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

        MockSubscriber<AskPrivat24OtpToCreateNewLink> askOtpHandler = new MockSubscriber<>(AskPrivat24OtpToCreateNewLink.class);
        bus.register(askOtpHandler);

        subject.refreshAuthentication(bankLink.id);

        assertEquals(operationId, askOtpHandler.getEvent().operationId);
    }
}
