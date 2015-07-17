package com.infora.ledger.banks;

import android.test.AndroidTestCase;

import com.infora.ledger.TestHelper;
import com.infora.ledger.api.DeviceSecret;
import com.infora.ledger.banks.ua.privatbank.PrivatBankLinkData;
import com.infora.ledger.banks.ua.privatbank.PrivatBankTransaction;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.Entity;
import com.infora.ledger.mocks.MockDatabaseContext;
import com.infora.ledger.mocks.MockPrivatBankApi;
import com.infora.ledger.mocks.MockUnitOfWork;
import com.infora.ledger.support.Dates;
import com.infora.ledger.support.SystemDate;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by jenya on 09.06.15.
 */
public class DefaultFetchStrategyTest extends AndroidTestCase {

    private DefaultFetchStrategy subject;
    private MockDatabaseContext mockDb;
    private BankLink bankLink;
    private PrivatBankLinkData linkData;
    private MockPrivatBankApi mockApi;
    private Date now;
    private DeviceSecret secret;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockApi = new MockPrivatBankApi();
        subject = new DefaultFetchStrategy(mockApi);
        mockDb = new MockDatabaseContext();
        now = SystemDate.setNow(TestHelper.randomDate());

        Calendar lastSyncDate = Calendar.getInstance();
        lastSyncDate.setTime(now);
        lastSyncDate.add(Calendar.DAY_OF_MONTH, -5);

        linkData = new PrivatBankLinkData("card-100", "merchant-100", "password-100");
        secret = DeviceSecret.generateNew();
        bankLink = new BankLink()
                .setAccountId("account-100")
                .setBic("bic-100")
                .setLastSyncDate(lastSyncDate.getTime())
                .setLinkData(linkData, secret);

        Calendar dateFrom = Calendar.getInstance();
        dateFrom.setTime(bankLink.lastSyncDate);
        dateFrom.add(Calendar.DAY_OF_MONTH, 1);

        mockApi.expectedGetTransactionsRequest = new GetTransactionsRequest(bankLink, dateFrom.getTime(), now);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        SystemDate.setNow(null);
    }

    public void testSetProgressFlags() throws FetchException {

        MockUnitOfWork.Hook hook1 = new MockUnitOfWork.Hook() {
            @Override
            public void onCommitting(MockUnitOfWork mockUnitOfWork) {
                assertEquals(1, mockUnitOfWork.attachedEntities.size());
                assertTrue(mockUnitOfWork.attachedEntities.contains(bankLink));
                assertTrue(bankLink.isInProgress);
                assertFalse(bankLink.hasSucceed);
                super.onCommitting(mockUnitOfWork);
            }
        };
        mockDb.addUnitOfWorkHook(hook1);
        MockUnitOfWork.Hook hook2 = new MockUnitOfWork.Hook() {
            @Override
            public void onCommitting(MockUnitOfWork mockUnitOfWork) {
                assertEquals(1, mockUnitOfWork.attachedEntities.size());
                assertTrue(mockUnitOfWork.attachedEntities.contains(bankLink));
                assertFalse(bankLink.isInProgress);
                assertTrue(bankLink.hasSucceed);
                assertEquals(now, bankLink.lastSyncDate);
                super.onCommitting(mockUnitOfWork);
            }
        };
        mockDb.addUnitOfWorkHook(hook2);
        subject.fetchBankTransactions(mockDb, bankLink, secret);
        hook1.assertCommitted();
        hook2.assertCommitted();
    }

    public void testFetchTransactionsIfLastFetchDateWasToday() throws Exception {
        final Date now = Dates.parse("yyyy-MM-dd HH:mm:ss", "2015-05-23 21:50:23");
        SystemDate.setNow(now);
        bankLink.lastSyncDate = now;

        mockApi.expectedGetTransactionsRequest.startDate = Dates.startOfDay(now);
        mockApi.expectedGetTransactionsRequest.endDate = now;
        final BankTransaction t1 = new PrivatBankTransaction()
                .setCard("card-100")
                .setTrandate("2015-05-23")
                .setTrantime("21:56:23")
                .setCardamount("-100.31 UAH")
                .setTerminal("terminal-1")
                .setDescription("description-1");
        final BankTransaction t2 = new PrivatBankTransaction()
                .setCard("card-101")
                .setTrandate("2015-05-23")
                .setTrantime("21:56:24")
                .setCardamount("443.33 UAH")
                .setTerminal("terminal-2")
                .setDescription("description-2");

        mockApi.bankTransactions.add(t1);
        mockApi.bankTransactions.add(t2);

        mockDb.addUnitOfWorkHook(new MockUnitOfWork.Hook());
        MockUnitOfWork.Hook hook2 = new MockUnitOfWork.Hook() {
            @Override
            public void onCommitted(MockUnitOfWork mockUnitOfWork) {
                MockUnitOfWork.Commit commit = mockUnitOfWork.commits.get(0);
                assertEquals(2, commit.addedEntities.size());
            }
        };
        mockDb.addUnitOfWorkHook(hook2);
        subject.fetchBankTransactions(mockDb, bankLink, secret);
        hook2.assertCommitted();
    }

    public void testFetchTransactionsFromNextDateAfterLastTillNow() throws FetchException {
        final Date now = Dates.set(new Date(), 2015, 4, 30, 2, 1, 30);
        SystemDate.setNow(now);
        bankLink.lastSyncDate = Dates.addDays(now, -11);
        Date dateFrom = Dates.addDays(now, -10);

        mockApi.expectedGetTransactionsRequest.startDate = dateFrom;
        mockApi.expectedGetTransactionsRequest.endDate = now;
        final BankTransaction t1 = new PrivatBankTransaction()
                .setCard("card-100")
                .setTrandate("2015-05-20")
                .setTrantime("21:56:23")
                .setCardamount("-100.31 UAH")
                .setTerminal("terminal-1")
                .setDescription("description-1");
        final BankTransaction t2 = new PrivatBankTransaction()
                .setCard("card-101")
                .setTrandate("2015-05-24")
                .setTrantime("21:56:23")
                .setCardamount("443.33 UAH")
                .setTerminal("terminal-2")
                .setDescription("description-2");

        mockApi.bankTransactions.add(t1);
        mockApi.bankTransactions.add(t2);

        mockDb.addUnitOfWorkHook(new MockUnitOfWork.Hook());
        MockUnitOfWork.Hook hook2 = new MockUnitOfWork.Hook() {
            @Override
            public void onCommitted(MockUnitOfWork mockUnitOfWork) {
                assertEquals(1, mockUnitOfWork.commits.size());
                MockUnitOfWork.Commit commit = mockUnitOfWork.commits.get(0);
                assertEquals(2, commit.addedEntities.size());
                assertTrue(commit.addedEntities.contains(t1.toPendingTransaction(bankLink)));
                assertTrue(commit.addedEntities.contains(t2.toPendingTransaction(bankLink)));
            }
        };
        mockDb.addUnitOfWorkHook(hook2);
        subject.fetchBankTransactions(mockDb, bankLink, secret);
        hook2.assertCommitted();
    }

    public void testIgnoreExistingTransactions() throws FetchException {
        final Date now = Dates.set(new Date(), 2015, 4, 30, 2, 1, 30);
        SystemDate.setNow(now);
        bankLink.lastSyncDate = Dates.addDays(now, -11);
        Date dateFrom = Dates.addDays(now, -10);

        mockApi.expectedGetTransactionsRequest.startDate = dateFrom;
        mockApi.expectedGetTransactionsRequest.endDate = now;
        final BankTransaction t1 = new PrivatBankTransaction()
                .setCard("card-100")
                .setTrandate("2015-05-20")
                .setTrantime("21:56:23")
                .setCardamount("-100.31 UAH")
                .setTerminal("terminal-1")
                .setDescription("description-1");
        final BankTransaction t2 = new PrivatBankTransaction()
                .setCard("card-101")
                .setTrandate("2015-05-24")
                .setTrantime("21:56:23")
                .setCardamount("443.33 UAH")
                .setTerminal("terminal-2")
                .setDescription("description-2");

        mockApi.bankTransactions.add(t1);
        mockApi.bankTransactions.add(t2);

        mockDb.mockTransactionsReadModel.injectAnd(t1.toPendingTransaction(bankLink))
                .injectAnd(t2.toPendingTransaction(bankLink));
        mockDb.addUnitOfWorkHook(new MockUnitOfWork.Hook());
        mockDb.addUnitOfWorkHook(new MockUnitOfWork.Hook() {
            @Override
            public <TEntity extends Entity> void onAddNew(TEntity entity) {
                fail("It is not expected that new entities are added. Got: " + entity);
            }
        });
        subject.fetchBankTransactions(mockDb, bankLink, secret);
    }
}