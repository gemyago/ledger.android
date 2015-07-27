package com.infora.ledger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.test.RenamingDelegatingContext;
import android.test.mock.MockContentResolver;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;

import com.infora.ledger.application.commands.DeleteTransactionsCommand;
import com.infora.ledger.application.commands.ReportTransactionCommand;
import com.infora.ledger.application.events.TransactionReportedEvent;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.mocks.BarrierSubscriber;
import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.MockMenuItem;
import com.infora.ledger.mocks.MockPendingTransactionsContentProvider;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.support.BusUtils;
import com.infora.ledger.support.SharedPreferencesUtil;

import java.util.concurrent.BrokenBarrierException;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 21.03.15.
 */
public class ReportActivityTest extends android.test.ActivityUnitTestCase<ReportActivity> {

    private EventBus bus;
    private MockPendingTransactionsContentProvider mockProvider;

    public ReportActivityTest() {
        super(ReportActivity.class);
    }

    @Override
    protected void setActivity(Activity testActivity) {
        if (testActivity != null) testActivity.setTheme(R.style.AppTheme);
        super.setActivity(testActivity);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        final Context baseContext = getInstrumentation().getTargetContext();
        SharedPreferences.Editor edit = SharedPreferencesUtil.getDefaultSharedPreferences(baseContext).edit();
        edit.remove(SettingsFragment.KEY_DEFAULT_ACCOUNT_ID).commit();

        bus = new EventBus();
        final MockLedgerApplication app = new MockLedgerApplication(baseContext, bus);
        MockContentResolver mockContentResolver = new MockContentResolver(app);
        mockProvider = new MockPendingTransactionsContentProvider(app);
        mockContentResolver.addProvider(TransactionContract.AUTHORITY, mockProvider);
        app.mockContentResolver = mockContentResolver;
        setActivityContext(app);
        startActivity(new Intent(), null, null);
        getActivity().doNotCallRequestSync = true;
    }
    
    public void testRequestSyncOnStart() {
        EventBus bus = new EventBus();
        BusUtils.setBus(getActivity(), bus);
        MockSubscriber<ReportActivity.RequestSyncCommand> mockSubscriber = new MockSubscriber<>(ReportActivity.RequestSyncCommand.class);
        bus.register(mockSubscriber);
        getInstrumentation().callActivityOnStart(getActivity());
        assertEquals("Request sync command hasn't been posted", 1, mockSubscriber.getEvents().size());
        assertFalse("Is manual flag was mistakenly set", mockSubscriber.getEvent().isManual);
    }

    public void testRequestSyncOnSynchronizeAction() {
        EventBus bus = new EventBus();
        BusUtils.setBus(getActivity(), bus);
        MockSubscriber<ReportActivity.RequestSyncCommand> mockSubscriber = new MockSubscriber<>(ReportActivity.RequestSyncCommand.class);
        bus.register(mockSubscriber);
        getActivity().onOptionsItemSelected(new MockMenuItem(R.id.action_synchronize));
        assertEquals("Request sync command hasn't been posted", 1, mockSubscriber.getEvents().size());
        assertTrue("Is manual flag wasn't set", mockSubscriber.getEvent().isManual);
    }

    public void testReportNewTransactionOnReportButtonClick() {
        EventBus bus = new EventBus();
        BusUtils.setBus(getActivity(), bus);
        MockSubscriber<ReportTransactionCommand> subscriber = new MockSubscriber<>(ReportTransactionCommand.class);
        bus.register(subscriber);
        Window wnd = getActivity().getWindow();
        EditText amount = (EditText) wnd.findViewById(R.id.amount);
        amount.setText("100.22");
        EditText comment = (EditText) wnd.findViewById(R.id.comment);
        comment.setText("Commenting transaction 100.22");

        View reportButton = wnd.findViewById(R.id.report);
        reportButton.callOnClick();

        ReportTransactionCommand cmd = subscriber.getEvent();
        assertNotNull(cmd);
        assertNull(cmd.accountId);
        assertEquals(amount.getText().toString(), cmd.getAmount());
        assertEquals(comment.getText().toString(), cmd.getComment());
        assertFalse("The report button was not disabled", reportButton.isEnabled());
    }

    public void testReportNewTransactionWithDefaultAccount() {
        SharedPreferences prefs = SharedPreferencesUtil.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SettingsFragment.KEY_DEFAULT_ACCOUNT_ID, "account-100");
        editor.apply();

        EventBus bus = new EventBus();
        BusUtils.setBus(getActivity(), bus);
        MockSubscriber<ReportTransactionCommand> subscriber = new MockSubscriber<>(ReportTransactionCommand.class);
        bus.register(subscriber);
        Window wnd = getActivity().getWindow();
        ((EditText) wnd.findViewById(R.id.amount)).setText("100.22");

        wnd.findViewById(R.id.report).callOnClick();

        ReportTransactionCommand cmd = subscriber.getEvent();
        assertNotNull(cmd);
        assertEquals("account-100", cmd.accountId);
    }

    public void testReportNewTransactionOnImeCommentEditorAction() {
        EventBus bus = new EventBus();
        BusUtils.setBus(getActivity(), bus);
        MockSubscriber<ReportTransactionCommand> subscriber = new MockSubscriber<>(ReportTransactionCommand.class);
        bus.register(subscriber);
        Window wnd = getActivity().getWindow();
        EditText amount = (EditText) wnd.findViewById(R.id.amount);
        amount.setText("100.22");
        EditText comment = (EditText) wnd.findViewById(R.id.comment);
        comment.setText("Commenting transaction 100.22");

        int imeActionReportId = getActivity().getResources().getInteger(R.integer.ime_action_report);
        comment.onEditorAction(imeActionReportId);

        View reportButton = wnd.findViewById(R.id.report);
        ReportTransactionCommand cmd = subscriber.getEvent();
        assertNotNull(cmd);
        assertEquals(amount.getText().toString(), cmd.getAmount());
        assertEquals(comment.getText().toString(), cmd.getComment());
        assertFalse("The report button was not disabled", reportButton.isEnabled());
    }

    public void testReportNewTransactionEmptyAmount() {
        EventBus bus = new EventBus();
        BusUtils.setBus(getActivity(), bus);
        MockSubscriber<ReportTransactionCommand> subscriber = new MockSubscriber<>(ReportTransactionCommand.class);
        bus.register(subscriber);
        Window wnd = getActivity().getWindow();
        View reportButton = wnd.findViewById(R.id.report);
        reportButton.callOnClick();
        assertNull("The command was dispatched but should not", subscriber.getEvent());
    }

    public void testTransactionReportedEvent() {
        EventBus bus = new EventBus();
        BusUtils.setBus(getActivity(), bus);
        MockSubscriber<ReportTransactionCommand> subscriber = new MockSubscriber<>(ReportTransactionCommand.class);
        bus.register(subscriber);
        Window wnd = getActivity().getWindow();
        EditText amount = (EditText) wnd.findViewById(R.id.amount);
        amount.setText("100.22");
        EditText comment = (EditText) wnd.findViewById(R.id.comment);
        comment.setText("Commenting transaction 100.22");
        View reportButton = wnd.findViewById(R.id.report);
        reportButton.setEnabled(false);

        getActivity().onEventMainThread(new TransactionReportedEvent(100));

        assertEquals("", amount.getText().toString());
        assertEquals("", comment.getText().toString());
        assertTrue("The report button was not enabled", reportButton.isEnabled());
    }

    public void testDeleteAction() throws BrokenBarrierException, InterruptedException {
        mockProvider.setQueryResult(
                new PendingTransaction(1, "100", "Comment 100"),
                new PendingTransaction(2, "101", "Comment 101"),
                new PendingTransaction(3, "102", "Comment 102")
        );
        BarrierSubscriber<ReportActivity.TransactionsLoaded> barrier = new BarrierSubscriber<>(ReportActivity.TransactionsLoaded.class);
        bus.register(barrier);
        getInstrumentation().callActivityOnStart(getActivity());
        barrier.await();
        ListView transactionsList = (ListView) getActivity().findViewById(R.id.reported_transactions_list);
        assertEquals(3, transactionsList.getAdapter().getCount());
        transactionsList.setItemChecked(0, true);
        transactionsList.setItemChecked(2, true);

        MockSubscriber<DeleteTransactionsCommand> deleteHandler = new MockSubscriber<>(DeleteTransactionsCommand.class);
        bus.register(deleteHandler);
        getActivity().findViewById(R.id.menu_delete).callOnClick();
        assertNotNull(deleteHandler.getEvent());
        long[] deletedIds = deleteHandler.getEvent().getIds();
        assertEquals(2, deletedIds.length);
        assertEquals(1, deletedIds[0]);
        assertEquals(3, deletedIds[1]);
    }
}