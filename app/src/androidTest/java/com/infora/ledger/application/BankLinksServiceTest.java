package com.infora.ledger.application;

import android.test.AndroidTestCase;

import com.infora.ledger.application.commands.AddBankLinkCommand;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.mocks.MockBankLinkData;
import com.infora.ledger.mocks.MockBankLinksRepository;
import com.infora.ledger.mocks.MockSubscriber;

import java.sql.SQLException;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 31.05.15.
 */
public class BankLinksServiceTest extends AndroidTestCase {

    private BankLinksService subject;
    private MockBankLinksRepository repository;
    private EventBus bus;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        bus = new EventBus();
        repository = new MockBankLinksRepository();
        subject = new BankLinksService(bus, repository);
    }

    public void testAddBankLinkCommand() throws SQLException {
        AddBankLinkCommand command = new AddBankLinkCommand();
        command.accountId = "account-100";
        command.accountName = "Account 100";
        command.bic = "bank-100";
        command.linkData = new MockBankLinkData("login-332", "password-332");

        MockSubscriber<BankLinkAdded> subscriber = new MockSubscriber<>();
        bus.register(subscriber);

        subject.onEventBackgroundThread(command);

        assertEquals(1, repository.savedBankLinks.size());
        assertTrue(repository.savedBankLinks.contains(new BankLink()
                        .setAccountId("account-100")
                        .setAccountName("Account 100")
                        .setBic("bank-100")
                        .setLinkData(command.linkData)
        ));

        assertEquals(1, subscriber.getEvents().size());
        assertEquals("account-100", subscriber.getEvent().accountId);
        assertEquals("bank-100", subscriber.getEvent().bic);
    }
}