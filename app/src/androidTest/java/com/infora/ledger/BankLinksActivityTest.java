package com.infora.ledger;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.widget.ListView;

import com.infora.ledger.application.commands.DeleteBankLinksCommand;
import com.infora.ledger.data.BankLink;
import com.infora.ledger.data.DatabaseRepository;
import com.infora.ledger.data.DatabaseContext;
import com.infora.ledger.mocks.BarrierSubscriber;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.support.BusUtils;

import java.sql.SQLException;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 01.06.15.
 */
public class BankLinksActivityTest extends android.test.ActivityUnitTestCase<BankLinksActivity> {

    private EventBus bus;
    private DatabaseRepository<BankLink> repo;

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
        bus = new EventBus();
        startActivity(new Intent(), null, null);
        BusUtils.setBus(getActivity(), bus);

        repo = new DatabaseContext(getActivity()).createRepository(BankLink.class);
        DbUtils.deleteAllDatabases(getActivity());
    }

    public void testBankLinksLoaded() throws SQLException {
        BankLink link1 = repo.save(new BankLink().setBic("bank-1").setAccountId("account-1").setAccountName("Account 1").setLinkData("dummy"));
        BankLink link2 = repo.save(new BankLink().setBic("bank-2").setAccountId("account-2").setAccountName("Account 2").setLinkData("dummy"));
        BankLink link3 = repo.save(new BankLink().setBic("bank-3").setAccountId("account-3").setAccountName("Account 3").setLinkData("dummy"));

        BarrierSubscriber<BankLinksActivity.BankLinksLoaded> barrier = new BarrierSubscriber<>(BankLinksActivity.BankLinksLoaded.class);
        bus.register(barrier);
        getInstrumentation().callActivityOnStart(getActivity());
        barrier.await();

        ListView bankLinksList = (ListView) getActivity().findViewById(R.id.bank_links_list);
        assertEquals(3, bankLinksList.getAdapter().getCount());

        assertEquals(link1, new BankLink((Cursor) bankLinksList.getAdapter().getItem(0)));
        assertEquals(link2, new BankLink((Cursor) bankLinksList.getAdapter().getItem(1)));
        assertEquals(link3, new BankLink((Cursor) bankLinksList.getAdapter().getItem(2)));
    }

    public void testEditBankLink() throws SQLException {
        repo.save(new BankLink().setBic("bank-1").setAccountId("account-1").setAccountName("Account 1").setLinkData("dummy"));
        BankLink link2 = repo.save(new BankLink().setBic("bank-2").setAccountId("account-2").setAccountName("Account 2").setLinkData("dummy"));

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
        BankLink link1 = repo.save(new BankLink().setBic("bank-1").setAccountId("account-1").setAccountName("Account 1").setLinkData("dummy"));
        repo.save(new BankLink().setBic("bank-2").setAccountId("account-2").setAccountName("Account 2").setLinkData("dummy"));
        BankLink link3 = repo.save(new BankLink().setBic("bank-3").setAccountId("account-3").setAccountName("Account 3").setLinkData("dummy"));

        BarrierSubscriber<BankLinksActivity.BankLinksLoaded> barrier = new BarrierSubscriber<>(BankLinksActivity.BankLinksLoaded.class);
        bus.register(barrier);
        getInstrumentation().callActivityOnStart(getActivity());
        barrier.await();

        ListView bankLinksList = (ListView) getActivity().findViewById(R.id.bank_links_list);
        bankLinksList.setItemChecked(0, true);
        bankLinksList.setItemChecked(2, true);

        MockSubscriber<DeleteBankLinksCommand> deleteHandler = new MockSubscriber<>(DeleteBankLinksCommand.class);
        bus.register(deleteHandler);
        getActivity().findViewById(R.id.menu_delete).callOnClick();
        assertNotNull(deleteHandler.getEvent());

        long[] deletedIds = deleteHandler.getEvent().ids;
        assertEquals(2, deletedIds.length);
        assertEquals(link1.id, deletedIds[0]);
        assertEquals(link3.id, deletedIds[1]);
    }
}