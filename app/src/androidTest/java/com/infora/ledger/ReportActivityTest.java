package com.infora.ledger;

import android.content.Context;
import android.content.Intent;
import android.test.mock.MockContentResolver;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.infora.ledger.application.commands.ReportTransactionCommand;
import com.infora.ledger.application.events.TransactionReportedEvent;
import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.MockMenuItem;
import com.infora.ledger.mocks.MockPendingTransactionsContentProvider;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.support.BusUtils;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 21.03.15.
 */
public class ReportActivityTest extends android.test.ActivityUnitTestCase<ReportActivity> {

    private EventBus bus;

    public ReportActivityTest() {
        super(ReportActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        final Context baseContext = getInstrumentation().getTargetContext();
        bus = new EventBus();
        final MockLedgerApplication app = new MockLedgerApplication(baseContext, bus);
        MockContentResolver mockContentResolver = new MockContentResolver(app);
        mockContentResolver.addProvider(TransactionContract.AUTHORITY, new MockPendingTransactionsContentProvider(app));
        app.mockContentResolver = mockContentResolver;
        setActivityContext(app);
        startActivity(new Intent(), null, null);
        getActivity().doNotCallRequestSync = true;
    }
    
    public void testRequestSyncOnStart() {
        EventBus bus = new EventBus();
        BusUtils.setBus(getActivity(), bus);
        MockSubscriber<ReportActivity.RequestSyncCommand> mockSubscriber = new MockSubscriber<>();
        bus.register(mockSubscriber);
        getInstrumentation().callActivityOnStart(getActivity());
        assertEquals("Request sync command hasn't been posted", 1, mockSubscriber.getEvents().size());
        assertFalse("Is manual flag was mistakenly set", mockSubscriber.getEvent().isManual);
    }

    public void testRequestSyncOnSynchronizeAction() {
        EventBus bus = new EventBus();
        BusUtils.setBus(getActivity(), bus);
        MockSubscriber<ReportActivity.RequestSyncCommand> mockSubscriber = new MockSubscriber<>();
        bus.register(mockSubscriber);
        getActivity().onOptionsItemSelected(new MockMenuItem(R.id.action_synchronize));
        assertEquals("Request sync command hasn't been posted", 1, mockSubscriber.getEvents().size());
        assertTrue("Is manual flag wasn't set", mockSubscriber.getEvent().isManual);
    }

    public void testReportNewTransactionOnReportButtonClick() {
        EventBus bus = new EventBus();
        BusUtils.setBus(getActivity(), bus);
        MockSubscriber<ReportTransactionCommand> subscriber = new MockSubscriber<>();
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
        assertEquals(amount.getText().toString(), cmd.getAmount());
        assertEquals(comment.getText().toString(), cmd.getComment());
        assertFalse("The report button was not disabled", reportButton.isEnabled());
    }

    public void testReportNewTransactionOnImeCommentEditorAction() {
        EventBus bus = new EventBus();
        BusUtils.setBus(getActivity(), bus);
        MockSubscriber<ReportTransactionCommand> subscriber = new MockSubscriber<>();
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
        MockSubscriber<ReportTransactionCommand> subscriber = new MockSubscriber<>();
        bus.register(subscriber);
        Window wnd = getActivity().getWindow();
        View reportButton = wnd.findViewById(R.id.report);
        reportButton.callOnClick();
        assertNull("The command was dispatched but should not", subscriber.getEvent());
    }

    public void testTransactionReportedEvent() {
        EventBus bus = new EventBus();
        BusUtils.setBus(getActivity(), bus);
        MockSubscriber<ReportTransactionCommand> subscriber = new MockSubscriber<>();
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
}