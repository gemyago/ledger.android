package com.infora.ledger;

import android.accounts.Account;
import android.test.AndroidTestCase;

import com.infora.ledger.application.commands.CreateSystemAccountCommand;
import com.infora.ledger.mocks.MockAccountManagerWrapper;

/**
 * Created by jenya on 21.03.15.
 */
public class LedgerApplicationTest extends AndroidTestCase {

    private LedgerApplication subject;
    private MockAccountManagerWrapper accountManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        subject = new LedgerApplication();
        accountManager = new MockAccountManagerWrapper(subject);
        subject.setAccountManager(accountManager);
    }

    public void testCreateSystemAccountCommand() {
        subject.onEvent(new CreateSystemAccountCommand("test@mail.com"));
        assertNotNull(accountManager.getAddAccountExplicitlyArgs());
        Account account = accountManager.getAddAccountExplicitlyArgs().getAccount();
        assertEquals("test@mail.com", account.name);
        assertEquals(LedgerApplication.ACCOUNT_TYPE, account.type);
        assertNull(accountManager.getAddAccountExplicitlyArgs().getUserdata());
    }
}
