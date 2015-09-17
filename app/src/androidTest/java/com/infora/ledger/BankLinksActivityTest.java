package com.infora.ledger;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.widget.ListView;

import com.infora.ledger.application.commands.DeleteBankLinksCommand;
import com.infora.ledger.application.commands.FetchBankTransactionsCommand;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.mocks.BarrierSubscriber;
import com.infora.ledger.mocks.MockActionMode;
import com.infora.ledger.mocks.MockDatabaseContext;
import com.infora.ledger.mocks.MockDatabaseRepository;
import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.MockMenuItem;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.mocks.di.TestApplicationModule;

import java.sql.SQLException;
import java.util.Date;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 01.06.15.
 */
public class BankLinksActivityTest extends android.test.ActivityUnitTestCase<BankLinksActivity> {

    @Inject EventBus bus;
    private MockDatabaseRepository<BankLink> repo;

    public BankLinksActivityTest() {
        super(BankLinksActivity.class);
    }

    @Override
    protected void setActivity(Activity testActivity) {
        if (testActivity != null) testActivity.setTheme(R.style.AppTheme);
        super.setActivity(testActivity);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        MockLedgerApplication app = new MockLedgerApplication(getInstrumentation().getTargetContext())
                .withInjectorModuleInit(new MockLedgerApplication.InjectorModuleInit() {
                    @Override public void init(TestApplicationModule module) {
                        module.databaseContext = new MockDatabaseContext()
                                .addMockRepo(BankLink.class, repo = new MockDatabaseRepository(BankLink.class));
                    }
                });
        app.injector().inject(this);
        setActivityContext(app);
        startActivity(new Intent(), null, null);
    }

    public void testBankLinksLoaded() throws SQLException {
        BankLink link1 = new BankLink().setId(101).setBic("bank-1").setAccountId("account-1").setAccountName("Account 1").setLinkDataValue("dummy").setLastSyncDate(new Date());
        BankLink link2 = new BankLink().setId(102).setBic("bank-2").setAccountId("account-2").setAccountName("Account 2").setLinkDataValue("dummy").setLastSyncDate(new Date());
        BankLink link3 = new BankLink().setId(103).setBic("bank-3").setAccountId("account-3").setAccountName("Account 3").setLinkDataValue("dummy").setLastSyncDate(new Date());

        repo.all.add(link1);
        repo.all.add(link2);
        repo.all.add(link3);

        BarrierSubscriber<BankLinksActivity.BankLinksLoaded> barrier = new BarrierSubscriber<>(BankLinksActivity.BankLinksLoaded.class);
        bus.register(barrier);
        getInstrumentation().callActivityOnStart(getActivity());
        barrier.await();

        ListView bankLinksList = (ListView) getActivity().findViewById(R.id.bank_links_list);
        assertEquals(3, bankLinksList.getAdapter().getCount());

        Cursor cursor1 = (Cursor) bankLinksList.getAdapter().getItem(0);
        assertEquals(link1.id, cursor1.getInt(cursor1.getColumnIndexOrThrow(BanksContract.BankLinks._ID)));
        assertEquals(link1.bic, cursor1.getString(cursor1.getColumnIndexOrThrow(BanksContract.BankLinks.COLUMN_BIC)));
        assertEquals(link1.accountName, cursor1.getString(cursor1.getColumnIndexOrThrow(BanksContract.BankLinks.COLUMN_ACCOUNT_NAME)));

        Cursor cursor2 = (Cursor) bankLinksList.getAdapter().getItem(1);
        assertEquals(link2.id, cursor2.getInt(cursor2.getColumnIndexOrThrow(BanksContract.BankLinks._ID)));
        assertEquals(link2.bic, cursor2.getString(cursor2.getColumnIndexOrThrow(BanksContract.BankLinks.COLUMN_BIC)));
        assertEquals(link2.accountName, cursor2.getString(cursor2.getColumnIndexOrThrow(BanksContract.BankLinks.COLUMN_ACCOUNT_NAME)));

        Cursor cursor3 = (Cursor) bankLinksList.getAdapter().getItem(2);
        assertEquals(link3.id, cursor3.getInt(cursor3.getColumnIndexOrThrow(BanksContract.BankLinks._ID)));
        assertEquals(link3.bic, cursor3.getString(cursor3.getColumnIndexOrThrow(BanksContract.BankLinks.COLUMN_BIC)));
        assertEquals(link3.accountName, cursor3.getString(cursor3.getColumnIndexOrThrow(BanksContract.BankLinks.COLUMN_ACCOUNT_NAME)));
    }

    public void testEditBankLink() throws SQLException {
        repo.all.add(new BankLink().setBic("bank-1").setAccountId("account-1").setAccountName("Account 1").setLinkDataValue("dummy").setLastSyncDate(new Date()));
        BankLink link2 = repo.save(new BankLink().setBic("bank-2").setAccountId("account-2").setAccountName("Account 2").setLinkDataValue("dummy").setLastSyncDate(new Date()));
        repo.all.add(link2);

        BarrierSubscriber<BankLinksActivity.BankLinksLoaded> barrier = new BarrierSubscriber<>(BankLinksActivity.BankLinksLoaded.class);
        bus.register(barrier);
        getInstrumentation().callActivityOnStart(getActivity());
        barrier.await();

        ListView bankLinksList = (ListView) getActivity().findViewById(R.id.bank_links_list);
        bankLinksList.performItemClick(bankLinksList.getChildAt(1), 1, link2.id);

        Intent intent = getStartedActivityIntent();
        assertNotNull(intent);
        assertEquals(EditBankLinkActivity.class.getName(), intent.getComponent().getClassName());
        assertEquals(link2.id, intent.getLongExtra(BankLinksActivity.BANK_LINK_ID_EXTRA, 0));
    }

    public void testDeleteBankLinks() throws SQLException {
        BankLink link1 = repo.save(new BankLink().setId(100).setBic("bank-1").setAccountId("account-1").setAccountName("Account 1").setLinkDataValue("dummy").setLastSyncDate(new Date()));
        repo.all.add(link1);
        repo.all.add(new BankLink().setId(101).setBic("bank-2").setAccountId("account-2").setAccountName("Account 2").setLinkDataValue("dummy").setLastSyncDate(new Date()));
        BankLink link3 = repo.save(new BankLink().setId(102).setBic("bank-3").setAccountId("account-3").setAccountName("Account 3").setLinkDataValue("dummy").setLastSyncDate(new Date()));
        repo.all.add(link3);

        BarrierSubscriber<BankLinksActivity.BankLinksLoaded> barrier = new BarrierSubscriber<>(BankLinksActivity.BankLinksLoaded.class);
        bus.register(barrier);
        getInstrumentation().callActivityOnStart(getActivity());
        barrier.await();

        ListView bankLinksList = getActivity().lvBankLinks;
        assertEquals(3, bankLinksList.getCount());
        bankLinksList.setItemChecked(0, true);
        bankLinksList.setItemChecked(2, true);

        MockSubscriber<DeleteBankLinksCommand> deleteHandler = new MockSubscriber<>(DeleteBankLinksCommand.class);
        bus.register(deleteHandler);
        getActivity().bankLinksChoiceListener.onActionItemClicked(new MockActionMode(), new MockMenuItem(R.id.menu_delete));
        assertNotNull(deleteHandler.getEvent());

        long[] deletedIds = deleteHandler.getEvent().ids;
        assertEquals(2, deletedIds.length);
        assertEquals(link1.id, deletedIds[0]);
        assertEquals(link3.id, deletedIds[1]);
    }

    public void testFetchTransactions() throws SQLException {
        BankLink link1 = repo.save(new BankLink().setId(100).setBic("bank-1").setAccountId("account-1").setAccountName("Account 1").setLinkDataValue("dummy").setLastSyncDate(new Date()));
        repo.all.add(link1);
        repo.all.add(new BankLink().setId(101).setBic("bank-2").setAccountId("account-2").setAccountName("Account 2").setLinkDataValue("dummy").setLastSyncDate(new Date()));
        BankLink link3 = repo.save(new BankLink().setId(102).setBic("bank-3").setAccountId("account-3").setAccountName("Account 3").setLinkDataValue("dummy").setLastSyncDate(new Date()));
        repo.all.add(link3);

        BarrierSubscriber<BankLinksActivity.BankLinksLoaded> barrier = new BarrierSubscriber<>(BankLinksActivity.BankLinksLoaded.class);
        bus.register(barrier);
        getInstrumentation().callActivityOnStart(getActivity());
        barrier.await();

        ListView bankLinksList = (ListView) getActivity().findViewById(R.id.bank_links_list);
        bankLinksList.setItemChecked(0, true);
        bankLinksList.setItemChecked(2, true);

        MockSubscriber<FetchBankTransactionsCommand> fetchHandler = new MockSubscriber<>(FetchBankTransactionsCommand.class);
        bus.register(fetchHandler);
        getActivity().findViewById(R.id.menu_fetch_bank_transactions).callOnClick();

        assertEquals(2, fetchHandler.getEvents().size());
        assertEquals(link1.id, fetchHandler.getEvents().get(0).bankLinkId);
        assertEquals(link3.id, fetchHandler.getEvents().get(1).bankLinkId);
    }

    public void testFetchAllTransactions() throws SQLException {
        BankLink link1 = repo.save(new BankLink().setId(101).setBic("bank-1").setAccountId("account-1").setAccountName("Account 1").setLinkDataValue("dummy").setLastSyncDate(new Date()));
        BankLink link2 = repo.save(new BankLink().setId(102).setBic("bank-2").setAccountId("account-2").setAccountName("Account 2").setLinkDataValue("dummy").setLastSyncDate(new Date()));

        repo.all.add(link1);
        repo.all.add(link2);

        BarrierSubscriber<BankLinksActivity.BankLinksLoaded> barrier = new BarrierSubscriber<>(BankLinksActivity.BankLinksLoaded.class);
        bus.register(barrier);
        getInstrumentation().callActivityOnStart(getActivity());
        barrier.await();

        MockSubscriber<FetchBankTransactionsCommand> fetchHandler = new MockSubscriber<>(FetchBankTransactionsCommand.class);
        bus.register(fetchHandler);
        getActivity().onOptionsItemSelected(new MockMenuItem(R.id.action_fetch_all_bank_links));

        assertEquals(2, fetchHandler.getEvents().size());
        assertEquals(link1.id, fetchHandler.getEvents().get(0).bankLinkId);
        assertEquals(link2.id, fetchHandler.getEvents().get(1).bankLinkId);
    }
}