package com.infora.ledger.ipc;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.test.AndroidTestCase;

import com.infora.ledger.application.events.Event;
import com.infora.ledger.mocks.MockLedgerApplication;
import com.infora.ledger.mocks.MockSubscriber;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

import static com.infora.ledger.ipc.EventsBroadcaster.EVENT_SENDER_PID;

/**
 * Created by mye on 9/18/2015.
 */
public class EventBroadcastsReceiverTest extends AndroidTestCase {

    private EventBroadcastsReceiver subject;
    private MockLedgerApplication app;

    @Inject EventBus bus;
    private MockEventsBroadcaster broadcaster;

    @Override protected void setUp() throws Exception {
        super.setUp();
        app = new MockLedgerApplication(getContext());
        app.injector().inject(this);
        subject = new EventBroadcastsReceiver();
        broadcaster = MockEventsBroadcaster.create();
    }

    public void testPostsBroadcastsFromOtherProcesses() throws Exception {
        broadcaster.onEvent(new DummyEvent("Event 1"));
        Intent broadcast = broadcaster.getRecentBroadcast();
        int senderPid = broadcast.getExtras().getInt(EVENT_SENDER_PID);
        broadcast.putExtra(EVENT_SENDER_PID, senderPid + 10);

        MockSubscriber<DummyEvent> subscriber = new MockSubscriber<>(DummyEvent.class);
        bus.register(subscriber);
        subject.onReceive(app, broadcast);

        assertEquals(1, subscriber.getEvents().size());
        assertEquals("Event 1", subscriber.getEvent().prop);
    }

    public void testSetBroadcastHeader() throws Exception {
        broadcaster.onEvent(new DummyEvent("Event 1"));
        Intent broadcast = broadcaster.getRecentBroadcast();
        int senderPid = broadcast.getExtras().getInt(EVENT_SENDER_PID);
        broadcast.putExtra(EVENT_SENDER_PID, senderPid + 10);

        MockSubscriber<DummyEvent> subscriber = new MockSubscriber<>(DummyEvent.class);
        bus.register(subscriber);
        subject.onReceive(app, broadcast);

        DummyEvent event = subscriber.getEvent();
        assertTrue(event.headers.containsKey(Event.HEADER_BROADCAST));
    }

    public void testDoesNotPostBroadcastsFromOwnProcess() throws Exception {
        broadcaster.onEvent(new DummyEvent("Event 1"));
        Intent broadcast = broadcaster.getRecentBroadcast();
        MockSubscriber<DummyEvent> subscriber = new MockSubscriber<>(DummyEvent.class);
        bus.register(subscriber);
        subject.onReceive(app, broadcast);
        assertEquals(0, subscriber.getEvents().size());
    }

    private static abstract class MockEventsBroadcaster extends EventsBroadcaster {
        public abstract Intent getRecentBroadcast();
        public MockEventsBroadcaster(Context context) {
            super(context);
        }
        public static MockEventsBroadcaster create() {
            final Intent[] recentBroadcast = new Intent[1];
            final ContextWrapper context = new ContextWrapper(null) {
                @Override public void sendBroadcast(Intent intent) {
                    recentBroadcast[0] = intent;
                }
            };
            return new MockEventsBroadcaster(context) {
                @Override public Intent getRecentBroadcast() {
                    return recentBroadcast[0];
                }
            };
        }
    }

    private static class DummyEvent extends Event {
        public String prop;

        public DummyEvent(String prop) {
            this.prop = prop;
        }
    }
}