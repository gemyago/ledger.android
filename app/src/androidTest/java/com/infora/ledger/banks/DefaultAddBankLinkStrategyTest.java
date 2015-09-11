package com.infora.ledger.banks;

import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.application.events.AddBankLinkFailed;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.mocks.MockDatabaseRepository;
import com.infora.ledger.mocks.MockSubscriber;

import junit.framework.TestCase;

import java.sql.SQLException;

import de.greenrobot.event.EventBus;

/**
 * Created by mye on 9/11/2015.
 */
public class DefaultAddBankLinkStrategyTest extends TestCase {

    private EventBus bus;
    private MockDatabaseRepository repository;
    private AddBankLinkStrategy subject;
    private DeviceSecret deviceSecret;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        bus = new EventBus();
        repository = new MockDatabaseRepository(BankLink.class);
        subject = new DefaultAddBankLinkStrategy();
        deviceSecret = DeviceSecret.generateNew();
    }

    public void testAddBankLink() {
        BankLink bankLink = new BankLink().setAccountId("account-1").setBic("bic-1");

        MockSubscriber<BankLinkAdded> addedHandler = new MockSubscriber<>(BankLinkAdded.class);
        bus.register(addedHandler);

        subject.addBankLink(bus, repository, bankLink, deviceSecret);
        assertEquals(1, repository.savedEntities.size());
        assertTrue(repository.savedEntities.contains(bankLink));

        BankLinkAdded addedEvent = addedHandler.getEvent();
        assertEquals("account-1", addedEvent.accountId);
        assertEquals("bic-1", addedEvent.bic);
    }

    public void testFailedToAddBankLink() {
        BankLink bankLink = new BankLink().setAccountId("account-1").setBic("bic-1");

        MockSubscriber<AddBankLinkFailed> failedHandler = new MockSubscriber<>(AddBankLinkFailed.class);
        bus.register(failedHandler);
        repository.saveException = new SQLException("Failed to add bank link");

        subject.addBankLink(bus, repository, bankLink, deviceSecret);

        AddBankLinkFailed failedEvent = failedHandler.getEvent();
        assertSame(repository.saveException, failedEvent.exception);
    }
}
