package com.infora.ledger;

import android.accounts.Account;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.infora.ledger.application.commands.DeleteTransactionsCommand;
import com.infora.ledger.application.commands.ReportTransactionCommand;
import com.infora.ledger.application.events.TransactionAdjusted;
import com.infora.ledger.application.events.TransactionReportedEvent;
import com.infora.ledger.application.events.TransactionsDeletedEvent;
import com.infora.ledger.application.synchronization.SynchronizationStrategiesFactory;
import com.infora.ledger.data.PendingTransaction;
import com.infora.ledger.mocks.BarrierSubscriber;
import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.MockMenuItem;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.mocks.MockSyncService;
import com.infora.ledger.mocks.MockTransactionsReadModel;
import com.infora.ledger.mocks.di.TestApplicationModule;
import com.infora.ledger.support.SharedPreferencesUtil;

import java.util.concurrent.BrokenBarrierException;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 21.03.15.
 */
public class ReportActivityTest extends android.test.ActivityUnitTestCase<ReportActivity> {

    @Inject EventBus bus;
    private MockTransactionsReadModel transactionsReadModel;
    private MockSyncService mockSyncService;

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

        final MockLedgerApplication app = new MockLedgerApplication(baseContext)
                .withInjectorModuleInit(new MockLedgerApplication.InjectorModuleInit() {
                    @Override public void init(TestApplicationModule module) {
                        module.transactionsReadModel = transactionsReadModel = new MockTransactionsReadModel();
                        mockSyncService = new MockSyncService();
                        module.syncService = mockSyncService;
                    }
                });
        app.injector().inject(this);
        setActivityContext(app);
        startActivity(new Intent(), null, null);
    }

    public void testTransactionsLoaded() {
        transactionsReadModel
                .injectAnd(new PendingTransaction(1, "100", "Comment 100"))
                .injectAnd(new PendingTransaction(2, "101", "Comment 101"))
                .injectAnd(new PendingTransaction(3, "102", "Comment 102"));

        BarrierSubscriber<ReportActivity.TransactionsLoaded> barrier = new BarrierSubscriber<>(ReportActivity.TransactionsLoaded.class);
        bus.register(barrier);
        getInstrumentation().callActivityOnStart(getActivity());
        barrier.await();

        ListAdapter adapter = getActivity().lvReportedTransactions.getAdapter();
        assertEquals(3, adapter.getCount());
        Cursor t1 = (Cursor) adapter.getItem(0);
        assertEquals(1, t1.getInt(t1.getColumnIndexOrThrow(TransactionContract._ID)));
        assertEquals("100", t1.getString(t1.getColumnIndexOrThrow(TransactionContract.COLUMN_AMOUNT)));
        assertEquals("Comment 100", t1.getString(t1.getColumnIndexOrThrow(TransactionContract.COLUMN_COMMENT)));

        Cursor t2 = (Cursor) adapter.getItem(1);
        assertEquals(2, t2.getInt(t2.getColumnIndexOrThrow(TransactionContract._ID)));
        assertEquals("101", t2.getString(t2.getColumnIndexOrThrow(TransactionContract.COLUMN_AMOUNT)));
        assertEquals("Comment 101", t2.getString(t2.getColumnIndexOrThrow(TransactionContract.COLUMN_COMMENT)));

        Cursor t3 = (Cursor) adapter.getItem(2);
        assertEquals(3, t3.getInt(t3.getColumnIndexOrThrow(TransactionContract._ID)));
        assertEquals("102", t3.getString(t3.getColumnIndexOrThrow(TransactionContract.COLUMN_AMOUNT)));
        assertEquals("Comment 102", t3.getString(t3.getColumnIndexOrThrow(TransactionContract.COLUMN_COMMENT)));
    }

    public void testRequestSyncOnStart() {
        MockSubscriber<ReportActivity.RequestSyncCommand> mockSubscriber = new MockSubscriber<>(ReportActivity.RequestSyncCommand.class);
        bus.register(mockSubscriber);
        getInstrumentation().callActivityOnStart(getActivity());
        assertEquals("Request sync command hasn't been posted", 1, mockSubscriber.getEvents().size());
        assertFalse("Is manual flag was mistakenly set", mockSubscriber.getEvent().isManual);
        assertTrue("Is ledger web flag wasn't set", mockSubscriber.getEvent().isLedgerWebOnly);
    }

    public void testRequestSyncOnSynchronizeAction() {
        MockSubscriber<ReportActivity.RequestSyncCommand> mockSubscriber = new MockSubscriber<>(ReportActivity.RequestSyncCommand.class);
        bus.register(mockSubscriber);
        getActivity().onOptionsItemSelected(new MockMenuItem(R.id.action_synchronize));
        assertEquals("Request sync command hasn't been posted", 1, mockSubscriber.getEvents().size());
        assertTrue("Is manual flag wasn't set", mockSubscriber.getEvent().isManual);
        assertFalse("Is ledger web was mistakenly set", mockSubscriber.getEvent().isLedgerWebOnly);
    }

    public void testReportNewTransactionOnReportButtonClick() {
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
        MockSubscriber<ReportTransactionCommand> subscriber = new MockSubscriber<>(ReportTransactionCommand.class);
        bus.register(subscriber);
        Window wnd = getActivity().getWindow();
        View reportButton = wnd.findViewById(R.id.report);
        reportButton.callOnClick();
        assertNull("The command was dispatched but should not", subscriber.getEvent());
    }

    public void testTransactionReportedEvent() {
        BarrierSubscriber<ReportActivity.TransactionsLoaded> barrier = new BarrierSubscriber<>(ReportActivity.TransactionsLoaded.class);
        bus.register(barrier);
        getInstrumentation().callActivityOnStart(getActivity());
        barrier.await();


        MockSubscriber<ReportTransactionCommand> subscriber = new MockSubscriber<>(ReportTransactionCommand.class);
        bus.register(subscriber);
        Window wnd = getActivity().getWindow();
        EditText amount = (EditText) wnd.findViewById(R.id.amount);
        amount.setText("100.22");
        EditText comment = (EditText) wnd.findViewById(R.id.comment);
        comment.setText("Commenting transaction 100.22");
        View reportButton = wnd.findViewById(R.id.report);
        reportButton.setEnabled(false);

        final boolean[] syncRequested = {false};
        mockSyncService.onRequestSync = new MockSyncService.OnRequestSync() {
            @Override public void call(Account account, String authority, Bundle extras) {
                assertNull(account);
                assertEquals(TransactionContract.AUTHORITY, authority);
                assertTrue(extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL));
                assertTrue(extras.getBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED));
                assertTrue(extras.getBoolean(ContentResolver.SYNC_EXTRAS_DO_NOT_RETRY));
                assertTrue(extras.getBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_BACKOFF));
                assertEquals(100, extras.getInt(SynchronizationStrategiesFactory.OPTION_PUBLISH_REPORTED_TRANSACTION));
                syncRequested[0] = true;
            }
        };

        barrier = new BarrierSubscriber<>(ReportActivity.TransactionsLoaded.class);
        bus.register(barrier);
        getActivity().onEventMainThread(new TransactionReportedEvent(100));
        barrier.await();
        assertTrue(syncRequested[0]);

        assertEquals("", amount.getText().toString());
        assertEquals("", comment.getText().toString());
        assertTrue("The report button was not enabled", reportButton.isEnabled());
    }

    public void testTransactionAdjusted() {
        BarrierSubscriber<ReportActivity.TransactionsLoaded> barrier = new BarrierSubscriber<>(ReportActivity.TransactionsLoaded.class);
        bus.register(barrier);
        getInstrumentation().callActivityOnStart(getActivity());
        barrier.await();

        barrier = new BarrierSubscriber<>(ReportActivity.TransactionsLoaded.class);
        bus.register(barrier);
        getActivity().onEventMainThread(new TransactionAdjusted(100));
        barrier.await();
    }

    public void testDeleteAction() throws BrokenBarrierException, InterruptedException {
        transactionsReadModel
                .injectAnd(new PendingTransaction(1, "100", "Comment 100"))
                .injectAnd(new PendingTransaction(2, "101", "Comment 101"))
                .injectAnd(new PendingTransaction(3, "102", "Comment 102"));

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
        long[] deletedIds = deleteHandler.getEvent().ids;
        assertEquals(2, deletedIds.length);
        assertEquals(1, deletedIds[0]);
        assertEquals(3, deletedIds[1]);
    }

    public void testTransactionsDeletedEvent() {
        BarrierSubscriber<ReportActivity.TransactionsLoaded> barrier = new BarrierSubscriber<>(ReportActivity.TransactionsLoaded.class);
        bus.register(barrier);
        getInstrumentation().callActivityOnStart(getActivity());
        barrier.await();

        barrier = new BarrierSubscriber<>(ReportActivity.TransactionsLoaded.class);
        bus.register(barrier);
        getActivity().onEventMainThread(new TransactionsDeletedEvent(100));
        barrier.await();
    }
}