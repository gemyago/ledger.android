package com.infora.ledger;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.internal.view.menu.MenuItemImpl;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.infora.ledger.application.ReportTransactionCommand;
import com.infora.ledger.application.TransactionReportedEvent;
import com.infora.ledger.mocks.MockMenuItem;
import com.infora.ledger.mocks.MockSubscriber;
import com.infora.ledger.support.BusUtils;

import de.greenrobot.event.EventBus;

/**
 * Created by jenya on 21.03.15.
 */
public class ReportActivityTest extends android.test.ActivityUnitTestCase<ReportActivity> {

    public ReportActivityTest() {
        super(ReportActivity.class);
    }

    @Override
    protected void setActivity(Activity testActivity) {
        if (testActivity != null) testActivity.setTheme(R.style.AppTheme);
        super.setActivity(testActivity);
    }

    public void testRequestSyncOnStart() {
        startActivity(new Intent(), null, null);
        EventBus bus = new EventBus();
        BusUtils.setBus(getActivity(), bus);
        MockSubscriber<ReportActivity.RequestSyncCommand> mockSubscriber = new MockSubscriber<>();
        bus.register(mockSubscriber);
        getInstrumentation().callActivityOnStart(getActivity());
        assertEquals("Request sync command hasn't been posted", 1, mockSubscriber.getEvents().size());
        assertFalse("Is manual flag was mistakenly set", mockSubscriber.getEvent().isManual);
    }

    public void testRequestSyncOnSynchronizeAction() {
        startActivity(new Intent(), null, null);
        EventBus bus = new EventBus();
        BusUtils.setBus(getActivity(), bus);
        MockSubscriber<ReportActivity.RequestSyncCommand> mockSubscriber = new MockSubscriber<>();
        bus.register(mockSubscriber);
        getActivity().onOptionsItemSelected(new MockMenuItem(R.id.action_synchronize));
        assertEquals("Request sync command hasn't been posted", 1, mockSubscriber.getEvents().size());
        assertTrue("Is manual flag wasn't set", mockSubscriber.getEvent().isManual);
    }

    public void testReportNewTransaction() {
        startActivity(new Intent(), null, null);
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

    public void testReportNewTransactionEmptyAmount() {
        startActivity(new Intent(), null, null);
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
        startActivity(new Intent(), null, null);
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