package com.infora.ledger.banks;

import com.infora.ledger.application.events.AddBankLinkFailed;
import com.infora.ledger.application.events.BankLinkAdded;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.mocks.MockDatabaseContext;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.mocks.MockUnitOfWork;

import junit.framework.TestCase;

import java.sql.SQLException;

import de.greenrobot.event.EventBus;

/**
 * Created by mye on 9/11/2015.
 */
public class DefaultAddBankLinkStrategyTest extends TestCase {

    private EventBus bus;
    private AddBankLinkStrategy subject;
    private MockDatabaseContext db;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        bus = new EventBus();
        db = new MockDatabaseContext();
        subject = new DefaultAddBankLinkStrategy(bus, db);
    }

    public void testAddBankLink() {
        final BankLink bankLink = new BankLink().setAccountId("account-1").setBic("bic-1");

        MockSubscriber<BankLinkAdded> addedHandler = new MockSubscriber<>(BankLinkAdded.class);
        bus.register(addedHandler);

        MockUnitOfWork.Hook hook = db.addUnitOfWorkHook(new MockUnitOfWork.Hook() {
            @Override
            public void onCommitted(MockUnitOfWork mockUnitOfWork) {
                assertEquals(1, mockUnitOfWork.commits.size());
                assertEquals(1, mockUnitOfWork.commits.get(0).addedEntities.size());
                assertTrue(mockUnitOfWork.commits.get(0).addedEntities.contains(bankLink));
            }
        });

        subject.addBankLink(bankLink);
        hook.assertCommitted();

        BankLinkAdded addedEvent = addedHandler.getEvent();
        assertEquals("account-1", addedEvent.accountId);
        assertEquals("bic-1", addedEvent.bic);
    }

    public void testFailedToAddBankLink() {
        BankLink bankLink = new BankLink().setAccountId("account-1").setBic("bic-1");

        final SQLException saveException = new SQLException("Failed to add bank link");
        db.addUnitOfWorkHook(new MockUnitOfWork.Hook() {
            @Override
            public void onCommitting(MockUnitOfWork mockUnitOfWork) throws SQLException {
                throw saveException;
            }
        });

        MockSubscriber<AddBankLinkFailed> failedHandler = new MockSubscriber<>(AddBankLinkFailed.class);
        bus.register(failedHandler);

        subject.addBankLink(bankLink);

        AddBankLinkFailed failedEvent = failedHandler.getEvent();
        assertSame(saveException, failedEvent.exception);
    }
}
