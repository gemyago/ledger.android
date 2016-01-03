package com.infora.ledger.application;

import android.test.AndroidTestCase;
import android.test.mock.MockApplication;

import com.infora.ledger.TestHelper;
import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.application.commands.AddBankLinkCommand;
import com.infora.ledger.application.commands.DeleteBankLinksCommand;
import com.infora.ledger.application.commands.FetchBankTransactionsCommand;
import com.infora.ledger.application.commands.UpdateBankLinkCommand;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.application.events.BankLinkUpdated;
import com.infora.ledger.application.events.BankLinksDeleted;
import com.infora.ledger.application.events.BankTransactionsFetched;
import com.infora.ledger.application.events.FetchBankTransactionsFailed;
import com.infora.ledger.application.events.UpdateBankLinkFailed;
import com.infora.ledger.banks.AddBankLinkStrategiesFactory;
import com.infora.ledger.banks.AddBankLinkStrategy;
import com.infora.ledger.banks.FetchException;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.mocks.MockBankLinkData;
import com.infora.ledger.mocks.MockDatabaseContext;
import com.infora.ledger.mocks.MockDatabaseRepository;
import com.infora.ledger.mocks.MockDeviceSecretProvider;
import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.mocks.di.TestApplicationModule;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 31.05.15.
 */
public class BankLinksServiceTest extends AndroidTestCase {

    @Inject BankLinksService subject;
    private MockDatabaseRepository<BankLink> repository;
    @Inject EventBus bus;
    private MockDatabaseContext db;
    private Date fetchFromDate;
    private DeviceSecret secret;
    private MockDeviceSecretProvider secretProvider;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        db = new MockDatabaseContext();
        repository = new MockDatabaseRepository(BankLink.class);
        db.addMockRepo(BankLink.class, repository);
        secret = DeviceSecret.generateNew();
        secretProvider = new MockDeviceSecretProvider(secret);
        final MockLedgerApplication app = new MockLedgerApplication(getContext()).withInjectorModuleInit(new MockLedgerApplication.InjectorModuleInit() {
            @Override public void init(TestApplicationModule module) {
                module.databaseContext = db;
                module.deviceSecretProvider = secretProvider;
            }
        });
        app.injector().inject(this);
    }

    public void testAddBankLinkCommand() {
        final AddBankLinkCommand command = new AddBankLinkCommand();
        command.accountId = "account-100";
        command.accountName = "Account 100";
        command.bic = "bank-100";
        command.initialFetchDate = TestHelper.randomDate();
        command.linkData = new MockBankLinkData("login-332", "password-332");

        MockSubscriber<BankLinkAdded> subscriber = new MockSubscriber<>(BankLinkAdded.class);
        bus.register(subscriber);

        final boolean[] strategyUsed = {false};
        subject.setAddStrategies(new AddBankLinkStrategiesFactory(null) {
            @Override
            public AddBankLinkStrategy getStrategy(String bic) {
                if (bic == command.bic) return new AddBankLinkStrategy() {
                    @Override
                    public void addBankLink(BankLink bankLink) {
                        assertEquals(new BankLink()
                                .setAccountId("account-100")
                                .setAccountName("Account 100")
                                .setBic("bank-100")
                                .setLastSyncDate(command.initialFetchDate)
                                .setInitialSyncDate(command.initialFetchDate)
                                .setLinkData(command.linkData, secret)
                                , bankLink);
                        strategyUsed[0] = true;
                    }
                };
                throw new RuntimeException("Not supported");
            }
        });

        subject.onEventBackgroundThread(command);
        assertTrue("The strategy was not used", strategyUsed[0]);
    }

    public void testAddBankLinkCommandWithNullLinkData() {
        AddBankLinkCommand command = new AddBankLinkCommand();

        boolean exceptionThrown = false;
        try {
            subject.onEventBackgroundThread(command);
        } catch (IllegalArgumentException ex) {
            exceptionThrown = true;
            assertEquals("command.linkData can not be null.", ex.getMessage());
        }
        assertTrue(exceptionThrown);
    }

    public void testUpdateBankLinkCommand() {
        MockBankLinkData linkData = new MockBankLinkData("login-1", "password-1");
        Date initialLastSyncDate = TestHelper.randomDate();
        BankLink bankLink = new BankLink()
                .setId(100)
                .setAccountId("account-100").setAccountName("Account 100")
                .setBic("bank-100").setLinkData(linkData, secret).setLastSyncDate(initialLastSyncDate);
        MockSubscriber<BankLinkUpdated> updatedHandler = new MockSubscriber<>(BankLinkUpdated.class);
        bus.register(updatedHandler);
        repository.entitiesToGetById.add(bankLink);
        subject.onEventBackgroundThread(new UpdateBankLinkCommand(bankLink.id, "new-account-100", "New Account 100",
                new MockBankLinkData("login-100", "password-100")));
        assertEquals(1, repository.savedEntities.size());
        assertEquals(initialLastSyncDate, bankLink.lastSyncDate);
        assertTrue(repository.savedEntities.contains(bankLink));
        assertEquals("new-account-100", bankLink.accountId);
        assertEquals("New Account 100", bankLink.accountName);
        MockBankLinkData actualLinkData = bankLink.getLinkData(MockBankLinkData.class, secret);
        assertEquals("login-100", actualLinkData.login);
        assertEquals("password-100", actualLinkData.password);
        assertEquals(1, updatedHandler.getEvents().size());
        assertEquals(bankLink.id, updatedHandler.getEvents().get(0).id);
    }

    public void testUpdateBankLinkCommandWithFetchFromDate() {
        MockBankLinkData linkData = new MockBankLinkData("login-1", "password-1");
        Date initialLastSyncDate = TestHelper.randomDate();
        BankLink bankLink = new BankLink()
                .setId(100)
                .setAccountId("account-100").setAccountName("Account 100")
                .setBic("bank-100").setLinkData(linkData, secret).setLastSyncDate(initialLastSyncDate);
        MockSubscriber<BankLinkUpdated> updatedHandler = new MockSubscriber<>(BankLinkUpdated.class);
        bus.register(updatedHandler);
        repository.entitiesToGetById.add(bankLink);
        fetchFromDate = TestHelper.randomDate();
        subject.onEventBackgroundThread(new UpdateBankLinkCommand(bankLink.id, bankLink.accountId, bankLink.accountName, linkData)
                .setFetchFromDate(fetchFromDate));
        assertEquals(1, repository.savedEntities.size());
        assertEquals(fetchFromDate, bankLink.initialSyncDate);
        assertEquals(fetchFromDate, bankLink.lastSyncDate);
    }

    public void testUpdateBankLinkCommandFailed() {
        BankLink bankLink = new BankLink()
                .setId(100)
                .setAccountId("account-100")
                .setBic("bank-100").setLinkData(new MockBankLinkData("login-1", "password-1"), secret);
        MockSubscriber<UpdateBankLinkFailed> updateFailedHandler = new MockSubscriber<>(UpdateBankLinkFailed.class);
        bus.register(updateFailedHandler);
        repository.saveException = new SQLException("sql exception");
        repository.entitiesToGetById.add(bankLink);
        subject.onEventBackgroundThread(new UpdateBankLinkCommand(bankLink.id, "new-account-100", "New Account 100",
                new MockBankLinkData("new-login-1", "new-password-1")));
        assertEquals(0, repository.savedEntities.size());
        assertEquals(1, updateFailedHandler.getEvents().size());
        assertEquals(bankLink.id, updateFailedHandler.getEvents().get(0).id);
        assertEquals(repository.saveException, updateFailedHandler.getEvents().get(0).exception);
    }

    public void testDeleteBankLinksCommand() throws SQLException {
        MockSubscriber<BankLinksDeleted> subscriber = new MockSubscriber<>(BankLinksDeleted.class);
        bus.register(subscriber);
        subject.onEventBackgroundThread(new DeleteBankLinksCommand(new long[]{1, 2, 443}));
        assertEquals(3, repository.deletedIds.length);
        assertEquals(1, repository.deletedIds[0]);
        assertEquals(2, repository.deletedIds[1]);
        assertEquals(443, repository.deletedIds[2]);

        BankLinksDeleted deletedEvent = subscriber.getEvent();
        assertNotNull(deletedEvent);

        assertEquals(3, deletedEvent.ids.length);
        assertEquals(1, deletedEvent.ids[0]);
        assertEquals(2, deletedEvent.ids[1]);
        assertEquals(443, deletedEvent.ids[2]);
    }

    public void testFetchBankTransactionsCommand() {
        final BankLink bankLink = new BankLink().setId(100);
        repository.entitiesToGetById.add(bankLink);
        final boolean[] fetchPerformed = {false};
        subject = new BankLinksService(bus, db, new MockDeviceSecretProvider(secret)) {
            @Override
            public void fetchBankTransactions(BankLink bl) throws FetchException {
                assertSame(bankLink, bl);
                fetchPerformed[0] = true;
            }
        };
        MockSubscriber<BankTransactionsFetched> fetchedSubscriber = new MockSubscriber<>(BankTransactionsFetched.class);
        bus.register(fetchedSubscriber);
        subject.onEventBackgroundThread(new FetchBankTransactionsCommand(bankLink.id));
        assertTrue(fetchPerformed[0]);
        assertEquals(1, fetchedSubscriber.getEvents().size());
        assertEquals(bankLink, fetchedSubscriber.getEvent().bankLink);
    }

    public void testFetchAllBankLinks() throws FetchException {
        final BankLink link1 = new BankLink().setId(101);
        final BankLink link2 = new BankLink().setId(102);
        final BankLink link3 = new BankLink().setId(103);
        repository.all.add(link1);
        repository.all.add(link2);
        repository.all.add(link3);
        final ArrayList<BankLink> fetchedLinks = new ArrayList<>();
        subject = new BankLinksService(bus, db, new MockDeviceSecretProvider(secret)) {
            @Override
            public void fetchBankTransactions(BankLink link) throws FetchException {
                fetchedLinks.add(link);
            }
        };
        subject.fetchAllBankLinks();
        assertEquals(3, fetchedLinks.size());
        assertTrue(fetchedLinks.contains(link1));
        assertTrue(fetchedLinks.contains(link2));
        assertTrue(fetchedLinks.contains(link3));
    }

    public void testFetchBankTransactionsCommandFailed() {
        final BankLink bankLink = new BankLink().setId(100);
        repository.entitiesToGetById.add(bankLink);
        final FetchException fetchFailed = new FetchException(new Exception("fetch failed"));
        subject = new BankLinksService(bus, db, new MockDeviceSecretProvider(secret)) {
            @Override
            public void fetchBankTransactions(BankLink bl) throws FetchException {
                throw fetchFailed;
            }
        };
        MockSubscriber<BankTransactionsFetched> fetchedSubscriber = new MockSubscriber<>(BankTransactionsFetched.class);
        MockSubscriber<FetchBankTransactionsFailed> fetchFailedHandler = new MockSubscriber<>(FetchBankTransactionsFailed.class);
        bus.register(fetchedSubscriber);
        bus.register(fetchFailedHandler);
        subject.onEventBackgroundThread(new FetchBankTransactionsCommand(bankLink.id));
        assertEquals(0, fetchedSubscriber.getEvents().size());
        assertEquals(1, fetchFailedHandler.getEvents().size());
        assertEquals(bankLink.id, fetchFailedHandler.getEvent().bankLinkId);
        assertEquals(fetchFailed, fetchFailedHandler.getEvent().error);
    }
}