package com.infora.ledger.application;

import android.test.AndroidTestCase;

import com.infora.ledger.application.commands.AddBankLinkCommand;
import com.infora.ledger.application.commands.DeleteBankLinksCommand;
import com.infora.ledger.application.commands.UpdateBankLinkCommand;
import com.infora.ledger.application.events.AddBankLinkFailed;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.application.events.BankLinkUpdated;
import com.infora.ledger.application.events.BankLinksDeletedEvent;
import com.infora.ledger.application.events.UpdateBankLinkFailed;
import com.infora.ledger.banks.PrivatBankLinkData;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.mocks.MockBankLinkData;
import com.infora.ledger.mocks.MockDatabaseContext;
import com.infora.ledger.mocks.MockDatabaseRepository;
import com.infora.ledger.mocks.MockSubscriber;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 31.05.15.
 */
public class BankLinksServiceTest extends AndroidTestCase {

    private BankLinksService subject;
    private MockDatabaseRepository repository;
    private EventBus bus;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        bus = new EventBus();
        MockDatabaseContext db = new MockDatabaseContext();
        repository = new MockDatabaseRepository(BankLink.class);
        db.addMockRepo(BankLink.class, repository);
        subject = new BankLinksService(bus, db);
    }

    public void testAddBankLinkCommand() {
        AddBankLinkCommand command = new AddBankLinkCommand();
        command.accountId = "account-100";
        command.accountName = "Account 100";
        command.bic = "bank-100";
        command.initialFetchDate = new Date();
        command.linkData = new MockBankLinkData("login-332", "password-332");

        MockSubscriber<BankLinkAdded> subscriber = new MockSubscriber<>(BankLinkAdded.class);
        bus.register(subscriber);

        subject.onEventBackgroundThread(command);

        Calendar cal = Calendar.getInstance();
        cal.setTime(command.initialFetchDate);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        assertEquals(1, repository.savedEntities.size());
        assertTrue(repository.savedEntities.contains(new BankLink()
                        .setAccountId("account-100")
                        .setAccountName("Account 100")
                        .setBic("bank-100")
                        .setLastSyncDate(cal.getTime())
                        .setLinkData(command.linkData)
        ));

        assertEquals(1, subscriber.getEvents().size());
        assertEquals("account-100", subscriber.getEvent().accountId);
        assertEquals("bank-100", subscriber.getEvent().bic);
    }

    public void testAddBankLinkCommandFailed() {
        AddBankLinkCommand command = new AddBankLinkCommand();
        command.accountId = "account-100";
        command.accountName = "Account 100";
        command.bic = "bank-100";
        command.initialFetchDate = new Date();
        command.linkData = new MockBankLinkData("login-332", "password-332");

        MockSubscriber<AddBankLinkFailed> subscriber = new MockSubscriber<>(AddBankLinkFailed.class);
        bus.register(subscriber);

        SQLException saveFailure = new SQLException("Some failure");
        repository.saveException = saveFailure;
        subject.onEventBackgroundThread(command);

        assertEquals(0, repository.savedEntities.size());

        assertEquals(1, subscriber.getEvents().size());
        assertEquals(saveFailure, subscriber.getEvent().exception);
    }

    public void testUpdateBankLinkCommand() {
        PrivatBankLinkData linkData = new PrivatBankLinkData("card-1", "merchant-1", "password-1");
        BankLink bankLink = new BankLink()
                .setId(100)
                .setAccountId("account-100").setAccountName("Account 100")
                .setBic("bank-100").setLinkData(linkData);
        MockSubscriber<BankLinkUpdated> updatedHandler = new MockSubscriber<>(BankLinkUpdated.class);
        bus.register(updatedHandler);
        repository.entityToGetById = bankLink;
        subject.onEventBackgroundThread(new UpdateBankLinkCommand(bankLink.id, "new-account-100", "New Account 100",
                new PrivatBankLinkData("new-card-1", "new-merchant-1", "new-password-1")));
        assertEquals(1, repository.savedEntities.size());
        assertTrue(repository.savedEntities.contains(bankLink));
        assertEquals("new-account-100", bankLink.accountId);
        assertEquals("New Account 100", bankLink.accountName);
        PrivatBankLinkData actualLinkData = bankLink.getLinkData(PrivatBankLinkData.class);
        assertEquals("new-card-1", actualLinkData.card);
        assertEquals("new-merchant-1", actualLinkData.merchantId);
        assertEquals("new-password-1", actualLinkData.password);
        assertEquals(1, updatedHandler.getEvents().size());
        assertEquals(bankLink.id, updatedHandler.getEvents().get(0).id);
    }

    public void testUpdateBankLinkCommandFailed() {
        BankLink bankLink = new BankLink()
                .setId(100)
                .setAccountId("account-100")
                .setBic("bank-100").setLinkData(new PrivatBankLinkData("card-1", "merchant-1", "password-1"));
        MockSubscriber<UpdateBankLinkFailed> updateFailedHandler = new MockSubscriber<>(UpdateBankLinkFailed.class);
        bus.register(updateFailedHandler);
        repository.saveException = new SQLException("sql exception");
        repository.entityToGetById = bankLink;
        subject.onEventBackgroundThread(new UpdateBankLinkCommand(bankLink.id, "new-account-100", "New Account 100",
                new PrivatBankLinkData("new-card-1", "new-merchant-1", "new-password-1")));
        assertEquals(0, repository.savedEntities.size());
        assertEquals(1, updateFailedHandler.getEvents().size());
        assertEquals(bankLink.id, updateFailedHandler.getEvents().get(0).id);
        assertEquals(repository.saveException, updateFailedHandler.getEvents().get(0).exception);
    }

    public void testDeleteBankLinksCommand() throws SQLException {
        MockSubscriber<BankLinksDeletedEvent> subscriber = new MockSubscriber<>(BankLinksDeletedEvent.class);
        bus.register(subscriber);
        subject.onEventBackgroundThread(new DeleteBankLinksCommand(new long[]{1, 2, 443}));
        assertEquals(3, repository.deletedIds.length);
        assertEquals(1, repository.deletedIds[0]);
        assertEquals(2, repository.deletedIds[1]);
        assertEquals(443, repository.deletedIds[2]);

        BankLinksDeletedEvent deletedEvent = subscriber.getEvent();
        assertNotNull(deletedEvent);

        assertEquals(3, deletedEvent.ids.length);
        assertEquals(1, deletedEvent.ids[0]);
        assertEquals(2, deletedEvent.ids[1]);
        assertEquals(443, deletedEvent.ids[2]);
    }
}